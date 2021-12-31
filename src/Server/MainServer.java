package Server;

import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;

public class MainServer  {
    public static void main(String[] args){
        try {
            // create on port 1099
            Registry registry = LocateRegistry.createRegistry(1099);

            // create a new service named CommService
            registry.rebind("CommService", new CommunicatieImpl());
        } catch (Exception e) {
            e.printStackTrace();
        }
        System.out.println("System is ready");
    }
}
