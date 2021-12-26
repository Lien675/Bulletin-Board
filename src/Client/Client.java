package Client;

import Interface.Bump;
import Interface.Communicatie;
import Server.CommunicatieImpl;

import javax.crypto.SecretKey;
import javax.xml.bind.DatatypeConverter;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Map;
import java.util.Random;

import static Client.Crypto.*;

class Client {
    Random random = new Random((long) (Math.random() * 10));
    SecretKey eigenSecretKey;
    SecretKey partnersSecretKey;
    int eigenIndex;
    int partnersIndex;
    int eigenTag;
    int partnersTag;
    Bump bumpImpl;
    Communicatie impl;
    boolean gebumped = false;
    String naam;

    public Client(String naam, int poort, Communicatie com) throws Exception {
        try {
            // Zoek als al iemand verbinding wil maken
            // fire to localhost port
            Registry bumpRegistry = LocateRegistry.getRegistry("localhost", poort);
            // search for CommService
            this.bumpImpl = (Bump) bumpRegistry.lookup("BumpService");
        } catch (Exception e) {
            // Anders zelf hosten
            // create on port
            Registry registry = LocateRegistry.createRegistry(poort);
            // create a new service named CounterService
            registry.rebind("BumpService", new BumpImpl());
            // fire to localhost port
            Registry bumpRegistry = LocateRegistry.getRegistry("localhost", poort);
            // search for CommService
            this.bumpImpl = (Bump) bumpRegistry.lookup("BumpService");
        }

        eigenSecretKey = createAESKey();
        //random index voor eerste bericht
        eigenIndex = Math.abs(random.nextInt()); //bound moet waarschijnlijk = lengte van board
        //random tag voor bericht voor eerste bericht
        eigenTag = Math.abs(random.nextInt());

        this.naam = naam;
        this.impl = com;
    }

    //TODO: DEZE METHODE IS TIJDELIJK TER VERVANGING VAN EEN SHORT RANGE TRANSMISSION PROTOCOL
    public void clientBump() throws RemoteException {

        bumpImpl.bumpDeel1(eigenSecretKey, eigenIndex, eigenTag);

        Map<String, SecretKey> bumpResult = bumpImpl.bumpDeel2(eigenTag + "-" + eigenIndex);

        for (String partnerTagEnIndex : bumpResult.keySet()) {
            partnersSecretKey = bumpResult.get(partnerTagEnIndex);
            String[] split = partnerTagEnIndex.split("-");
            partnersTag = Integer.parseInt(split[0]);
            partnersIndex = Integer.parseInt(split[1]);
            System.out.println("Parnters key = " + (DatatypeConverter.printHexBinary(partnersSecretKey.getEncoded())));
        }

        gebumped = true;
    }

    public String clientReceive() throws Exception {

        //neem hash van partners tag
        MessageDigest digest = MessageDigest.getInstance("SHA-256");
        byte[] hashedTag = digest.digest(Integer.toString(partnersTag).getBytes());
        String hashedStringTag = DatatypeConverter.printHexBinary(hashedTag);

        //returned string zodra bericht aanwezig op plaats partnersIndex met key partnersTag
        byte[] u = impl.ontvangBericht(hashedStringTag, partnersIndex);

        //decrypt bericht
        String decryptedMessage = doAESDecryption(u, partnersSecretKey);
        String[] decryptedParts = decryptedMessage.split("-");
        System.out.println("DECRYPTED MESSAGE IN RECEIVE: " + decryptedMessage);
        //als er geen 3 delen aanwezig zijn, is er iets fout gegaan
        //if(decryptedParts.length!=3) return "";

        //haal delen uit bericht
        String message = decryptedParts[0];
        int nieuwePartnerIndex = Integer.parseInt(decryptedParts[1]);
        int nieuwePartnerTag = Integer.parseInt(decryptedParts[2]);

        //update partners waarden en key
        partnersIndex = nieuwePartnerIndex;
        partnersTag = nieuwePartnerTag;
        partnersSecretKey = deriveKey(partnersSecretKey);

        System.out.println("ONTVANGEN BERICHT: " + message);
        return message;
    }

    public void clientSend(String message) throws Exception {
        // Voeg naam toe aan bericht
        message = naam + ": " + message;

        //random index voor volgende bericht
        int idxab = Math.abs(random.nextInt()); //bound moet misschien = lengte van board
        //random tag voor bericht voor volgende bericht
        int tagab = Math.abs(random.nextInt());

        //versleutel message samen met index en tag van volgend bericht
        String allTogether = message + "-" + idxab + "-" + tagab;
//            byte[] initializationVector = createInitializationVector();
        byte[] cipherText = doAESEncryption(allTogether, eigenSecretKey);

        //hash tag:
        MessageDigest digest = MessageDigest.getInstance("SHA-256");
        byte[] hashedTag = digest.digest(Integer.toString(eigenTag).getBytes());
        String hashedStringTag = DatatypeConverter.printHexBinary(hashedTag);
        //int hashedTagInt = Integer.parseInt(DatatypeConverter.printHexBinary(hashedTag));

        //stuur bericht
        impl.stuurBericht(cipherText, hashedStringTag, eigenIndex);

        //update eigen waarden en key
        eigenIndex = idxab;
        eigenTag = tagab;
        eigenSecretKey = deriveKey(eigenSecretKey);
    }

}
