package Server;

import Interface.Communicatie;

import java.rmi.RemoteException;
import java.rmi.server.UnicastRemoteObject;
import java.util.*;

public class CommunicatieImpl extends UnicastRemoteObject implements Communicatie {
    Set<String> gebruikers = new HashSet<>();
    Map<String, Queue<String>> berichten = new HashMap<>();

    protected CommunicatieImpl() throws RemoteException {
    }

    @Override
    public synchronized void voegGebruikerToe(String naam) throws RemoteException {
        berichten.put(naam, new ArrayDeque<>());
        gebruikers.add(naam);
    }

    @Override
    // stuurt bericht naar de client
    public synchronized String ontvangBericht(String gebruiker) throws RemoteException {

        while (berichten.get(gebruiker).isEmpty()) {
            try {
                wait();
            } catch (InterruptedException e) {
                System.out.println(e.getStackTrace());
            }
        }

        StringBuilder sb = new StringBuilder();
        for (String bericht : berichten.get(gebruiker)) {
            sb.append(bericht);
            sb.append("\n");
        }
        berichten.get(gebruiker).clear();
        return sb.toString();
    }

    @Override
    // client stuurt bericht
    public synchronized void stuurBericht(String bericht, String naam) throws RemoteException {
        for (Queue<String> q : berichten.values()) {
            q.add(naam + ": " + bericht);
        }

        notifyAll();
    }
}
