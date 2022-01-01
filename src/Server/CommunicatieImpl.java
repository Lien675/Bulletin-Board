package Server;

import Interface.Communicatie;

import javax.xml.bind.DatatypeConverter;
import java.rmi.RemoteException;
import java.rmi.server.UnicastRemoteObject;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
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
                boardOld = new ArrayList<>(board);
                board = new ArrayList<>();
                for (int i = 0; i < currentSize + 10; i++) {
                    board.add(new HashMap<>());
                }
                twoBoards = true;
            } else if (isTooEmptyBoard(board) && !twoBoards) {
                boardOld = new ArrayList<>(board);
                differenceOld = difference;
                difference = 0;
                board = new ArrayList<>();
                for (int i = 0; i < currentSize - 10; i++) {
                    board.add(new HashMap<>());
                }
                twoBoards = true;
            }
        };
        ScheduledExecutorService executor = Executors.newScheduledThreadPool(1);
        executor.scheduleAtFixedRate(helloRunnable, 0, 1, TimeUnit.SECONDS);
    });

    public boolean isFullBoard(List<Map<String, byte[]>> board) {
        for (Map<String, byte[]> map : board) {
            if (map.isEmpty()) {
                return false;
            }
        }
        return true;
    }

    public boolean isTooEmptyBoard(List<Map<String, byte[]>> board) {
        int nrEmpty = 0;
        for (Map<String, byte[]> map : board) {
            if (map.isEmpty()) {
                nrEmpty++;
            }
        }
        return nrEmpty > 20;
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

        if (twoBoards && !alreadyCheckedInOld.contains(toAdd)) {
            alreadyCheckedInOld.add(toAdd);
            int moduloIndex = index % boardOld.size();
            boardOld.get(moduloIndex).get(tag);
            if (boardOld.get(moduloIndex).containsKey(tag)) {
                byte[] hashedBericht = boardOld.get(moduloIndex).get(tag);
                boardOld.get(moduloIndex).remove(tag);
                differenceOld--;
                if (differenceOld == 0) {
                    boardOld.clear();
                    twoBoards = false;
                    alreadyCheckedInOld.clear();
                }
                return hashedBericht;
            }
            //indien het gevraagde bericht niet in boardOld staat, zullen we in het gewone board zoeken en indien nodig wachten
        }

        int moduloIndex = index % board.size();
        board.get(moduloIndex).get(tag);

        if (!board.get(moduloIndex).containsKey(tag)) return null;

        byte[] hashedBericht = board.get(moduloIndex).get(tag);
        board.get(moduloIndex).remove(tag);
        difference--;
        return hashedBericht;
    }

    @Override
    // client stuurt bericht
    public synchronized void stuurBericht(byte[] bericht, String tag, int index) throws RemoteException {
        int moduloIndex = index % board.size();
        board.get(moduloIndex).put(tag, bericht);

        difference++;
    }

}
