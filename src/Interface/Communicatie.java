package Interface;

import java.rmi.Remote;
import java.rmi.RemoteException;

public interface Communicatie extends Remote {
    void voegGebruikerToe(String naam) throws RemoteException;
    String ontvangBericht(String gebruiker) throws RemoteException;
    void stuurBericht(String bericht, String naam) throws RemoteException;
}
