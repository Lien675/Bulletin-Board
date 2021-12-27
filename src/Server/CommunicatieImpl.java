package Server;

import Interface.Communicatie;

import javax.crypto.SecretKey;
import javax.xml.bind.DatatypeConverter;
import java.rmi.RemoteException;
import java.rmi.server.UnicastRemoteObject;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.*;

public class CommunicatieImpl extends UnicastRemoteObject implements Communicatie {
    List<Map<String, byte[]>> board = new ArrayList<>();

    protected CommunicatieImpl() throws RemoteException {
        for (int i = 0; i < 20; i++) {
            board.add(new HashMap<>());
        }
    }

    @Override
    // stuurt bericht naar de client
    public synchronized byte[] ontvangBericht(int preimage, int index) throws RemoteException, NoSuchAlgorithmException {
        //neem hash van partners tag
        MessageDigest digest = MessageDigest.getInstance("SHA-256");
        byte[] hashedTag = digest.digest(Integer.toString(preimage).getBytes());
        String tag = DatatypeConverter.printHexBinary(hashedTag);

        System.out.println("IN ONTVANG");
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
        notifyAll();
    }

}
