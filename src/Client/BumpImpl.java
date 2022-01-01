package Client;

import Interface.Bump;

import javax.crypto.SecretKey;
import java.rmi.RemoteException;
import java.rmi.server.UnicastRemoteObject;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

public class BumpImpl extends UnicastRemoteObject implements Bump {
    Map<String, SecretKey> bumpValues = new HashMap<>();

    protected BumpImpl() throws RemoteException {
    }

    @Override
    public synchronized void bumpDeel1(SecretKey eigenSecretKey, int eigenIndex, int eigenTag) throws RemoteException {
        String together = eigenTag + "-" + eigenIndex;
        bumpValues.put(together, eigenSecretKey);

        notifyAll();
    }

    @Override
    public synchronized Map<String, SecretKey> bumpDeel2(String eigenTagEnIndex) throws RemoteException {
        while (bumpValues.size() < 2) {
            try {
                wait();
            } catch (InterruptedException e) {
                System.out.println(Arrays.toString(e.getStackTrace()));
            }
        }

        for (String tag : bumpValues.keySet()) {
            if (!eigenTagEnIndex.contentEquals(tag)) {
                Map<String, SecretKey> tempMap = new HashMap<>();
                tempMap.put(tag, bumpValues.get(tag));
                return tempMap;
            }
        }

        return null;
    }
}
