package Client;

import Interface.Bump;
import Interface.Communicatie;

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
        System.out.println("IN BUMPDEEL1 MET TAG " + eigenTag);
        String together = eigenTag + "-" + eigenIndex;
        bumpValues.put(together, eigenSecretKey);

        notifyAll();
    }

    @Override
    public synchronized Map<String, SecretKey> bumpDeel2(String eigenTagEnIndex) throws RemoteException {
        System.out.println("IN BUMPDEEL2 MET TAG " + eigenTagEnIndex);
        while (bumpValues.size() < 2) {
            try {
                wait();
            } catch (InterruptedException e) {
                System.out.println(Arrays.toString(e.getStackTrace()));
            }
        }

        for (String tag : bumpValues.keySet()) {
            if (!eigenTagEnIndex.contentEquals(tag)) {
                System.out.println("BUMP GELUKT IN IMPL VOOR KLANT MET TAG = " + eigenTagEnIndex);
                Map<String, SecretKey> tempMap = new HashMap<>();
                tempMap.put(tag, bumpValues.get(tag));
//                bumpValues.remove(tag);
//                notifyAll();
                return tempMap;
            }
        }

        return null;
    }
}
