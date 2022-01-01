package Interface;

import java.rmi.Remote;
import java.rmi.RemoteException;
import java.security.NoSuchAlgorithmException;

public interface Communicatie extends Remote {
    byte[] ontvangBericht(int tag, int index) throws RemoteException, NoSuchAlgorithmException;

    void stuurBericht(byte[] bericht, String tag, int index) throws RemoteException;
}
