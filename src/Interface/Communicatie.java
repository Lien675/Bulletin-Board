package Interface;

import java.rmi.Remote;
import java.rmi.RemoteException;

public interface Communicatie extends Remote {
    //void voegGebruikerToe(String naam) throws RemoteException;
    String ontvangBericht(int tag, int index) throws RemoteException;
    void stuurBericht(String bericht, int tag, int index) throws RemoteException;

    void bump(String naam) throws RemoteException;
}
