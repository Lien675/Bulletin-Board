package Server;

import Interface.Communicatie;

import javax.crypto.SecretKey;
import javax.xml.bind.DatatypeConverter;
import java.rmi.RemoteException;
import java.rmi.server.UnicastRemoteObject;
import java.util.*;

public class CommunicatieImpl extends UnicastRemoteObject implements Communicatie {
//    Set<String> gebruikers = new HashSet<>();
//    Map<String, Queue<String>> berichten = new HashMap<>();

    List<Map<String, byte[]>> board = new ArrayList<>();

    protected CommunicatieImpl() throws RemoteException {
        for (int i = 0; i < 20; i++) {
            board.add(new HashMap<>());
        }
    }

    @Override
    // stuurt bericht naar de client
    public synchronized byte[] ontvangBericht(String tag, int index) throws RemoteException {

        int moduloIndex = index % board.size();
        board.get(moduloIndex).get(tag);

        //wacht als geen bericht aanwezig is op plaats index, met key = tag
        while (!board.get(moduloIndex).containsKey(tag)) {
            try {
                wait();
            } catch (InterruptedException e) {
                System.out.println(Arrays.toString(e.getStackTrace()));
            }
        }

        byte[] hashedBericht = board.get(moduloIndex).get(tag);
        board.get(moduloIndex).remove(tag);
        return hashedBericht;

//        while (berichten.get(gebruiker).isEmpty()) {
//            try {
//                wait();
//            } catch (InterruptedException e) {
//                System.out.println(e.getStackTrace());
//            }
//        }
//
//        StringBuilder sb = new StringBuilder();
//        for (String bericht : berichten.get(gebruiker)) {
//            sb.append(bericht);
//            sb.append("\n");
//        }
//        berichten.get(gebruiker).clear();
//        return sb.toString();

    }

    @Override
    // client stuurt bericht
    public synchronized void stuurBericht(byte[] bericht, String tag, int index) throws RemoteException {
//        for (Queue<String> q : berichten.values()) {
//            q.add(naam + ": " + bericht);
//        }
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

        notifyAll();
    }

}
