package Server;

import Interface.Communicatie;

import javax.crypto.SecretKey;
import javax.xml.bind.DatatypeConverter;
import java.rmi.RemoteException;
import java.rmi.server.UnicastRemoteObject;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.*;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

public class CommunicatieImpl extends UnicastRemoteObject implements Communicatie {
    List<Map<String, byte[]>> board = new ArrayList<>();
    List<Map<String, byte[]>> boardOld = new ArrayList<>();
    int difference = 0;
    int differenceOld = 0;
    int currentSize;
    boolean twoBoards;
    ArrayList<List<Integer>> alreadyCheckedInOld = new ArrayList<>();


    Thread changeBoardSizeThread = new Thread(() -> {
        Runnable helloRunnable = () -> {
            if (isFullBoard(board) && !twoBoards) {
                try {
                    boardOld = deepCopyOfBoard(board);
                    board = new ArrayList<>();
                    for (int i = 0; i < currentSize+10; i++) {
                        board.add(new HashMap<>());
                    }
                    twoBoards = true;
                    System.out.println("\\-------------------------------BOARD IS EXPANDED-------------------------------");
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
            else if(isTooEmptyBoard(board) && !twoBoards) {
                try {
                    boardOld = deepCopyOfBoard(board);
                    differenceOld = difference;
                    difference = 0;
                    board = new ArrayList<>();
                    for (int i = 0; i < currentSize-10; i++) {
                        board.add(new HashMap<>());
                    }
                    twoBoards = true;
                    System.out.println("\\-------------------------------BOARD IS SHRINKED-------------------------------");
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        };
        ScheduledExecutorService executor = Executors.newScheduledThreadPool(1);
        executor.scheduleAtFixedRate(helloRunnable, 0, 1, TimeUnit.MILLISECONDS);
    });

    public List<Map<String, byte[]>> deepCopyOfBoard(List<Map<String, byte[]>> board) {
        List<Map<String, byte[]>> newBoard = new ArrayList<>();
        for(Map map: board) {
            newBoard.add(map);
        }
        return newBoard;
    }

    public boolean isFullBoard(List<Map<String, byte[]>> board) {
        boolean full = true;
        for(Map<String, byte[]> map : board) {
            if(map.isEmpty()) {
                return false;
            }
        }
        return full;
    }

    public boolean isTooEmptyBoard(List<Map<String, byte[]>> board) {
        boolean tooEmpty = false;
        int nrEmpty = 0;
        for(Map<String, byte[]> map : board) {
            if(map.isEmpty()) {
                nrEmpty++;
            }
        }
        if(nrEmpty>20) {
            tooEmpty = true;
        }
        return tooEmpty;
    }

    protected CommunicatieImpl() throws RemoteException {
        //De initiële grootte van het bord is 20. Deze waarde is fixed, achteraf kan een groter of kleiner bord aangemaakt worden.
        currentSize = 20;
        for (int i = 0; i < currentSize; i++) {
            board.add(new HashMap<>());
        }
        twoBoards = false;
        changeBoardSizeThread.start();
    }

    @Override
    // stuurt bericht naar de client
    public synchronized byte[] ontvangBericht(int preimage, int index) throws RemoteException, NoSuchAlgorithmException {
        //neem hash van partners tag
        MessageDigest digest = MessageDigest.getInstance("SHA-256");
        byte[] hashedTag = digest.digest(Integer.toString(preimage).getBytes());
        String tag = DatatypeConverter.printHexBinary(hashedTag);
        List<Integer> toAdd = new ArrayList<>();
        toAdd.add(preimage);
        toAdd.add(index);

        if(twoBoards && !alreadyCheckedInOld.contains(toAdd)) {
            alreadyCheckedInOld.add(toAdd);
            System.out.println("\\-------------------------------CHECKING OLD BOARD-------------------------------");
            int moduloIndex = index % boardOld.size();
            boardOld.get(moduloIndex).get(tag);
            if(boardOld.get(moduloIndex).containsKey(tag)){
                byte[] hashedBericht = boardOld.get(moduloIndex).get(tag);
                boardOld.get(moduloIndex).remove(tag);
                differenceOld--;
                if(differenceOld==0) {
                    boardOld.clear();
                    twoBoards = false;
                    alreadyCheckedInOld.clear();
                    System.out.println("\\-------------------------------OLD BOARD IS REMOVED-------------------------------");
                }
                return hashedBericht;
            }
            //indien het gevraagde bericht niet in boardOld staat, zullen we in het gewone board zoeken en indien nodig wachten
        }


        int moduloIndex = index % board.size();
        board.get(moduloIndex).get(tag);

        //wacht als geen bericht aanwezig is op plaats index, met key = tag
        if(!board.get(moduloIndex).containsKey(tag)) return null;
        System.out.println("\\-------------------------------GETTING AN ACTUAL MESSAGE FROM ORIGIN BOARD-------------------------------");
//        while (!board.get(moduloIndex).containsKey(tag)) {
//            try {
//                wait();
//            } catch (InterruptedException e) {
//                System.out.println(Arrays.toString(e.getStackTrace()));
//            }
//        }

        byte[] hashedBericht = board.get(moduloIndex).get(tag);
        board.get(moduloIndex).remove(tag);
        difference--;
        return hashedBericht;
    }

    @Override
    // client stuurt bericht
    public synchronized void stuurBericht(byte[] bericht, String tag, int index) throws RemoteException {
        System.out.println("IN STUUR BERICHT MET BERICHT " + bericht);
        int moduloIndex = index % board.size();
        board.get(moduloIndex).put(tag, bericht);

        System.out.println("staat board:");
        for (Map<String, byte[]> map : board) {
            if (map.keySet().isEmpty()) System.out.println("deze rij is empty");
            else {
                for (String key : map.keySet()) {
                    System.out.print("key: " + key + " value: " + DatatypeConverter.printHexBinary(map.get(key)) + " ");
                }
                System.out.println();
            }

        }
        difference++;
        notifyAll();
    }

}
