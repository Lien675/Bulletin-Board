package Interface;

import java.rmi.Remote;
import java.rmi.RemoteException;

public interface Communicatie extends Remote {
    //void voegGebruikerToe(String naam) throws RemoteException;
    byte[] ontvangBericht(String tag, int index) throws RemoteException;
    void stuurBericht(byte[] bericht, String tag, int index ) throws RemoteException;
}
