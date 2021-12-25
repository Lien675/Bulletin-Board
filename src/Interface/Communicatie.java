package Interface;

import javax.crypto.SecretKey;
import java.rmi.Remote;
import java.rmi.RemoteException;
import java.util.Map;

public interface Communicatie extends Remote {
    //void voegGebruikerToe(String naam) throws RemoteException;
    byte[] ontvangBericht(String tag, int index) throws RemoteException;
    void stuurBericht(byte[] bericht, String tag, int index ) throws RemoteException;

    void bumpDeel1(SecretKey eigenSecretKey, int eigenIndex, int eigenTag) throws RemoteException;
    Map<String,SecretKey> bumpDeel2(String eigenTag) throws RemoteException;
    }
