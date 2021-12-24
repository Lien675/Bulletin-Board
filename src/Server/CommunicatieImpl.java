package Server;

import Interface.Communicatie;

import java.rmi.RemoteException;
import java.rmi.server.UnicastRemoteObject;
import java.util.*;
 
public class CommunicatieImpl extends UnicastRemoteObject implements Communicatie {
    Set<String> gebruikers = new HashSet<>();
    Map<String, Queue<String>> berichten = new HashMap<>();

    List<Map<Integer,String>> board = new ArrayList<>();


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
    public synchronized String ontvangBericht(int tag, int index) throws RemoteException {
        int moduloIndex = index % board.size();
        board.get(moduloIndex).get(tag);
        while (!board.get(moduloIndex).containsKey(tag)){
            try {
                wait();
            } catch (InterruptedException e) {
                System.out.println(e.getStackTrace());
            }
        }
        String hashedBericht = board.get(moduloIndex).get(tag);
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

    return "";
    }

    @Override
    // client stuurt bericht
    public synchronized void stuurBericht(String bericht, int tag, int index) throws RemoteException {
//        for (Queue<String> q : berichten.values()) {
//            q.add(naam + ": " + bericht);
//        }
        int moduloIndex = index % board.size();
        board.get(moduloIndex).put(tag,bericht);

        notifyAll();
    }

    public synchronized void bump(String naam) throws RemoteException{//naam van andere persoon waar je mee wil uitwisselen

    }
}
