package Client;

import Interface.Bump;
import Interface.Communicatie;

import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;
import javax.xml.bind.DatatypeConverter;
import java.io.IOException;
import java.rmi.ConnectException;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.security.MessageDigest;
import java.util.*;

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
    boolean isOnline = false;
    int poort;
    int aantalTokens = 5;


    public Client(String naam, int poort, Communicatie com) throws Exception {
        //check of client al ooit eens gebumped heeft

        boolean ooitAlGebumped = FileBeheer.bestaatFile(naam, poort);

        if(ooitAlGebumped){

            List<String> waarden = FileBeheer.readFromFile(naam, poort);
            setWaarden(waarden);
            gebumped=true;

        }

        else{
            try {
                // Zoek of al iemand verbinding wil maken
                // fire to localhost port
                Registry bumpRegistry = LocateRegistry.getRegistry("localhost", poort);
                // search for BumpService
                this.bumpImpl = (Bump) bumpRegistry.lookup("BumpService");
            } catch (ConnectException e) {
                // Anders zelf hosten
                // create on port
                Registry registry = LocateRegistry.createRegistry(poort);
                // create a new service named CounterService
                registry.rebind("BumpService", new BumpImpl());
                // fire to localhost port
                Registry bumpRegistry = LocateRegistry.getRegistry("localhost", poort);
                // search for BumpService
                this.bumpImpl = (Bump) bumpRegistry.lookup("BumpService");
            }

            eigenSecretKey = createAESKey();
            //random index voor eerste bericht
            eigenIndex = Math.abs(random.nextInt());
            //random tag voor bericht voor eerste bericht
            eigenTag = Math.abs(random.nextInt());
        }

        this.poort = poort;
        this.naam = naam;
        this.impl = com;
    }

    //TODO: DEZE METHODE IS TIJDELIJK TER VERVANGING VAN EEN SHORT RANGE TRANSMISSION PROTOCOL
    public void clientBump() throws IOException {

        bumpImpl.bumpDeel1(eigenSecretKey, eigenIndex, eigenTag);
        Map<String, SecretKey> bumpResult = bumpImpl.bumpDeel2(eigenTag + "-" + eigenIndex);

        for (String partnerTagEnIndex : bumpResult.keySet()) {
            partnersSecretKey = bumpResult.get(partnerTagEnIndex);
            String[] split = partnerTagEnIndex.split("-");
            partnersTag = Integer.parseInt(split[0]);
            partnersIndex = Integer.parseInt(split[1]);
            System.out.println("Parnters key = " + (DatatypeConverter.printHexBinary(partnersSecretKey.getEncoded())));
        }

        FileBeheer.writeToFile(getWaarenList(), naam, poort);

        gebumped = true;
    }

    public String clientReceive() throws Exception {
//        System.out.println("in client receive begin");
        //neem hash van partners tag
//        MessageDigest digest = MessageDigest.getInstance("SHA-256");
//        byte[] hashedTag = digest.digest(Integer.toString(partnersTag).getBytes());
//        String hashedStringTag = DatatypeConverter.printHexBinary(hashedTag);

        //returned string zodra bericht aanwezig op plaats partnersIndex met key partnersTag
//        byte[] u = impl.ontvangBericht(hashedStringTag, partnersIndex);
        byte[] u = impl.ontvangBericht(partnersTag, partnersIndex);

        if(u==null)return null;

        System.out.println("in client receive na ontvangBericht");
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

        FileBeheer.writeToFile(getWaarenList(), naam, poort);

        aantalTokens++;

        return message;
    }

    public void clientSend(String message) throws Exception {
        // Voeg naam toe aan bericht
        message = naam + ": " + message;
        message = message.replace('-', '_');

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

        FileBeheer.writeToFile(getWaarenList(), naam, poort);

        aantalTokens--;

    }

    private void setWaarden(List<String>waarden){

        String eigenSK = waarden.get(0);
        String partnerSK = waarden.get(1);
        String eigenInd = waarden.get(2);
        String partnerInd = waarden.get(3);
        String eigenT = waarden.get(4);
        String partnerT = waarden.get(5);


        // reconstrueer eigen key
        byte[] decodedKey1 = Base64.getDecoder().decode(eigenSK);
        eigenSecretKey = new SecretKeySpec(decodedKey1, 0, decodedKey1.length, "AES");
        //reconstrueer partner key
        byte[] decodedKey2 = Base64.getDecoder().decode(partnerSK);
        partnersSecretKey = new SecretKeySpec(decodedKey2, 0, decodedKey2.length, "AES");

        eigenIndex = Integer.parseInt(eigenInd);
        partnersIndex = Integer.parseInt(partnerInd);
        eigenTag = Integer.parseInt(eigenT);
        partnersTag = Integer.parseInt(partnerT);

    }

    public List<String> getWaarenList(){
        List<String> waarden = new ArrayList<>();

        String eigenKey = Base64.getEncoder().encodeToString(eigenSecretKey.getEncoded());
        String partnerKey = Base64.getEncoder().encodeToString(partnersSecretKey.getEncoded());

//
//        byte[] decodedKey1 = Base64.getDecoder().decode(eigenKey);
//        SecretKey gereconstruuerdeEigenKey = new SecretKeySpec(decodedKey1, 0, decodedKey1.length, "AES");

        waarden.add(eigenKey);
        waarden.add(partnerKey);
        waarden.add(Integer.toString(eigenIndex));
        waarden.add(Integer.toString(partnersIndex));
        waarden.add(Integer.toString(eigenTag));
        waarden.add(Integer.toString(partnersTag));

        return waarden;

    }

}
