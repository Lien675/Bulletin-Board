package Interface;

import javax.crypto.SecretKey;
import java.rmi.Remote;
import java.rmi.RemoteException;
import java.util.Map;

public interface Bump extends Remote {
    void bumpDeel1(SecretKey eigenSecretKey, int eigenIndex, int eigenTag) throws RemoteException;

    Map<String, SecretKey> bumpDeel2(String eigenTag) throws RemoteException;
}
