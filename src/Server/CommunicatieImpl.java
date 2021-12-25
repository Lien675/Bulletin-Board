package Server;

import Interface.Communicatie;

import javax.crypto.SecretKey;
import java.rmi.RemoteException;
import java.rmi.server.UnicastRemoteObject;
import java.util.*;

public class CommunicatieImpl extends UnicastRemoteObject implements Communicatie {
//    Set<String> gebruikers = new HashSet<>();
//    Map<String, Queue<String>> berichten = new HashMap<>();

    List<Map<String,byte[]>> board = new ArrayList<>();

    Map<String,SecretKey> bumpValues = new HashMap<>();

    protected CommunicatieImpl() throws RemoteException {
        for(int i=0;i<10;i++){
            board.add(new HashMap<>());
        }
    }

//    @Override
//    public synchronized void voegGebruikerToe(String naam) throws RemoteException {
//        berichten.put(naam, new ArrayDeque<>());
//        gebruikers.add(naam);
//    }

    @Override
    // stuurt bericht naar de client
    public synchronized byte[] ontvangBericht(String tag, int index) throws RemoteException {
        System.out.println("IN ONTVANG BERICHT IN IMPL VOOR TAG "+tag);

        int moduloIndex = index % board.size();
        board.get(moduloIndex).get(tag);

        //wacht als geen bericht aanwezig is op plaats index, met key = tag
        while (!board.get(moduloIndex).containsKey(tag)){
            try {
                wait();
            } catch (InterruptedException e) {
                System.out.println(Arrays.toString(e.getStackTrace()));
            }
        }

        System.out.println("UIT WAIT IN ONTVANG BERICHT GERAAKT");
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
        System.out.println("IN STUUR BERICHT MET BERICHT "+bericht);
        int moduloIndex = index % board.size();
        board.get(moduloIndex).put(tag,bericht);

        notifyAll();
    }



    public synchronized void bumpDeel1(SecretKey eigenSecretKey, int eigenIndex, int eigenTag) throws RemoteException {
        System.out.println("IN BUMPDEEL1 MET TAG "+eigenTag);
        String together =  eigenTag + "-" + eigenIndex;
        bumpValues.put(together,eigenSecretKey);

        notifyAll();
    }

    public synchronized Map<String,SecretKey> bumpDeel2(String eigenTagEnIndex) throws RemoteException{
        System.out.println("IN BUMPDEEL2 MET TAG "+eigenTagEnIndex);
        while (bumpValues.size()<2){
            try {
                wait();
            } catch (InterruptedException e) {
                System.out.println(Arrays.toString(e.getStackTrace()));
            }
        }

        for(String tag: bumpValues.keySet()){
            if(!eigenTagEnIndex.contentEquals(tag)){
                System.out.println("BUMP GELUKT IN IMPL VOOR KLANT MET TAG = "+eigenTagEnIndex);
                Map<String, SecretKey> tempMap = new HashMap<>();
                tempMap.put(tag,bumpValues.get(tag));
                return tempMap;
            }
        }


        return null;
    }

}
