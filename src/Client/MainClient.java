package Client;

import Interface.Communicatie;

import javax.crypto.Cipher;
import javax.crypto.KeyGenerator;
import javax.crypto.SecretKey;
import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.PBEKeySpec;
import javax.crypto.spec.SecretKeySpec;
import javax.swing.*;
import javax.xml.bind.DatatypeConverter;
import java.awt.*;
import java.nio.charset.StandardCharsets;
import java.rmi.NotBoundException;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.KeySpec;
import java.util.*;

public class  MainClient {


    public static final String AES = "AES";
    // We are using a Block cipher(CBC mode)
    private static final String AES_CIPHER_ALGORITHM = "AES/CBC/PKCS5PADDING";
    static Random random = new Random((long) (Math.random()*10));

    //TODO: FIX
    static final byte[] initializationVector = {-117, -123, 46, 60, 107, 12, 118, -119, -11, -59, 61, 124, -28, 53, 4, 43};//createInitializationVector();

    // Function to create a secret key
    public static SecretKey createAESKey() throws Exception {

        // Creating a new instance of
        // SecureRandom class.
        SecureRandom securerandom = new SecureRandom();

        // Passing the string to
        // KeyGenerator
        KeyGenerator keygenerator = KeyGenerator.getInstance(AES);

        // Initializing the KeyGenerator
        // with 256 bits.
        keygenerator.init(256, securerandom);
        SecretKey key = keygenerator.generateKey();

        System.out.println("KEY = "+DatatypeConverter.printHexBinary(key.getEncoded()));

        return key;
    }

    // Function to initialize a vector with an arbitrary value
    public static byte[] createInitializationVector() {

        // Used with encryption
        byte[] initializationVector = new byte[16];
        SecureRandom secureRandom = new SecureRandom();
        secureRandom.nextBytes(initializationVector);

        System.out.println("INITIALIZATION VECTOR: "+ Arrays.toString(initializationVector));

        return initializationVector;
    }

    // This function takes plaintext, the key with an initialization vector to convert plainTex into CipherText.
    public static byte[] do_AESEncryption(String plainText, SecretKey secretKey, byte[] initializationVector)
            throws Exception {
        Cipher cipher = Cipher.getInstance(AES_CIPHER_ALGORITHM);

        IvParameterSpec ivParameterSpec = new IvParameterSpec(initializationVector);

        cipher.init(Cipher.ENCRYPT_MODE, secretKey, ivParameterSpec);

        return cipher.doFinal(plainText.getBytes());
    }

    //This function performs the reverse operation of the do_AESEncryption function.
    // It converts ciphertext to the plaintext using the key.
    public static String do_AESDecryption(byte[] cipherText, SecretKey secretKey, byte[] initializationVector)
            throws Exception {
        Cipher cipher = Cipher.getInstance(AES_CIPHER_ALGORITHM);

        IvParameterSpec ivParameterSpec = new IvParameterSpec(initializationVector);

        cipher.init(Cipher.DECRYPT_MODE, secretKey, ivParameterSpec);

        byte[] result = cipher.doFinal(cipherText);

        return new String(result);
    }

    public static byte[] deriveKey(SecretKey pass, int keyLen) throws NoSuchAlgorithmException, InvalidKeySpecException {
        byte[] salt = new byte[64];

        String encodedKey = Base64.getEncoder().encodeToString(pass.getEncoded());
        char[] password = encodedKey.toCharArray();
        SecretKeyFactory kf = SecretKeyFactory.getInstance("PBKDF2WithHmacSHA1");
        KeySpec specs = new PBEKeySpec(password, salt, 1024, keyLen);
        SecretKey key = kf.generateSecret(specs);
        return key.getEncoded();
    }

//    public static void send(int index, int tag, String message, SecretKey Kab,Communicatie impl) throws Exception {
//        //random index voor volgende bericht
//        int idxab = random.nextInt(); //bound moet waarschijnlijk = lengte van board
//        //random tag voor bericht voor volgende bericht
//        int tagab = random.nextInt();
//
//        String allTogether = message +"-"+ idxab + "-"+ tagab;
//        byte[] initializationVector = createInitializationVector();
//        byte[] cipherText = do_AESEncryption(allTogether, Kab, initializationVector);
//
//        //hash tag:
//        MessageDigest digest = MessageDigest.getInstance("SHA-256");
//        byte[] hashedTag = digest.digest(Integer.toString(tag).getBytes());
//        int hashedTagInt = Integer.parseInt(DatatypeConverter.printHexBinary(hashedTag));
//        impl.stuurBericht(Arrays.toString(cipherText),hashedTagInt,index);
//
//        //TODO
//        //this.indexab = idxab
//        //this.tagab = tagab
//        //Kab = deriveKey(Kab)
//    }



//    public static String receive(SecretKey partnersKey, int partnersIndex, int partnersTag,Communicatie impl) throws Exception {
//        String u = impl.ontvangBericht(partnersTag,partnersIndex);
//
//        //TODO: dit is eigelijk fout, want moet gelijk zijn aan vector die andere gebruikte voor encriptie denk ik
//        byte[] initializationVector = createInitializationVector();
//        String decryptedMessage = do_AESDecryption(u.getBytes(StandardCharsets.UTF_8),partnersKey,initializationVector);
//        String[] decryptedParts = decryptedMessage.split("-");
//        if(decryptedParts.length!=3) return ""; //dan is er iets fout gegaan
//        String message = decryptedParts[0];
//        int nieuwePartnerIndex = Integer.parseInt(decryptedParts[1]);
//        int nieuwePartnerTag = Integer.parseInt(decryptedParts[2]);
//
//        //TODO
//        //this.partnersKey = deriveKey(partnersKey);
//        //this.partnersIndex = nieuwePartnerIndex;
//        //this.partnersTag = nieuwePartnerTag;
//        return message;
//    }

//    public static String[] bump(SecretKey eigenKey, int eigenIndex, int eigenTag,Communicatie impl){
//        String[] keyExchangeResult = new String[3];
//
//
//        keyExchangeResult = impl.bump();
//
//        return keyExchangeResult;
//    }

    static class Client{
        SecretKey eigenSecretKey;
        SecretKey partnersSecretKey;
        int eigenIndex;
        int partnersIndex;
        int eigenTag;
        int partnersTag;
        Communicatie impl;
        boolean gebumped;
        String naam = null;


        public Client(Communicatie com) throws Exception {
            eigenSecretKey = createAESKey();
            //random index voor eerste bericht
            eigenIndex = Math.abs( random.nextInt()); //bound moet waarschijnlijk = lengte van board
            //random tag voor bericht voor eerste bericht
            eigenTag = Math.abs( random.nextInt());

            impl = com;
            gebumped = false;
        }


        //TODO: DEZE METHODE IS TIJDELIJK TER VERVANGING VAN EEN SHORT RANGE TRANSMISSION PROTOCOL
        public void clientBump() throws RemoteException {

            impl.bumpDeel1(eigenSecretKey,eigenIndex,eigenTag);

            Map<String,SecretKey> bumpResult = impl.bumpDeel2(eigenTag + "-" + eigenIndex);

            for(String partnerTagEnIndex: bumpResult.keySet()){
                partnersSecretKey = bumpResult.get(partnerTagEnIndex);
                String[] split = partnerTagEnIndex.split("-");
                partnersTag = Integer.parseInt(split[0]);
                partnersIndex = Integer.parseInt(split[1]);
                System.out.println("Parnters key = "+(DatatypeConverter.printHexBinary(partnersSecretKey.getEncoded())));
            }

            gebumped = true;
        }

        public String clientReceive() throws Exception {

            //neem hash van partners tag
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] hashedTag = digest.digest(Integer.toString(partnersTag).getBytes());
            String hashedStringTag = DatatypeConverter.printHexBinary(hashedTag);
           // long hashedTagLong = Long.parseLong(DatatypeConverter.printHexBinary(hashedTag));

            //returned string zodra bericht aanwezig op plaats partnersIndex met key partnersTag
            byte[] u = impl.ontvangBericht(hashedStringTag,partnersIndex);

            //decrypt bericht
            String decryptedMessage = do_AESDecryption(u,partnersSecretKey,initializationVector);
            String[] decryptedParts = decryptedMessage.split("-");
            System.out.println("DECRYPTED MESSAGE IN RECEIVE: "+decryptedMessage);
            //als er geen 3 delen aanwezig zijn, is er iets fout gegaan
            //if(decryptedParts.length!=3) return "";

            //haal delen uit bericht
            String message = decryptedParts[0];
            int nieuwePartnerIndex = Integer.parseInt(decryptedParts[1]);
            int nieuwePartnerTag = Integer.parseInt(decryptedParts[2]);

            //update partners waarden en key
            partnersIndex = nieuwePartnerIndex;
            partnersTag = nieuwePartnerTag;
            byte[] newPartnersSecretKeyBytes = deriveKey(partnersSecretKey,192);
            partnersSecretKey = new SecretKeySpec(newPartnersSecretKeyBytes, 0, newPartnersSecretKeyBytes.length, "AES");


            System.out.println("ONTVANGEN BERICHT: "+message);
            return message;
        }

        public void clientSend(String message) throws Exception {

            //random index voor volgende bericht
            int idxab = Math.abs( random.nextInt()); //bound moet misschien = lengte van board
            //random tag voor bericht voor volgende bericht
            int tagab = Math.abs( random.nextInt());

            //versleutel message samen met index en tag van volgend bericht
            String allTogether = message +"-"+ idxab + "-"+ tagab;
//            byte[] initializationVector = createInitializationVector();
            byte[] cipherText = do_AESEncryption(allTogether, eigenSecretKey, initializationVector);

            //probeersel
            String temp = do_AESDecryption(cipherText,eigenSecretKey,initializationVector);
            System.out.println("DECRYPTION EMMEDIATLY AFTER ENCRYPTION: "+temp);

            //hash tag:
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] hashedTag = digest.digest(Integer.toString(eigenTag).getBytes());
            String hashedStringTag = DatatypeConverter.printHexBinary(hashedTag);
            //int hashedTagInt = Integer.parseInt(DatatypeConverter.printHexBinary(hashedTag));

            //stuur bericht
            impl.stuurBericht(cipherText,hashedStringTag,eigenIndex);

            //update eigen waarden en key
            eigenIndex = idxab;
            eigenTag = tagab;
            byte[] newEigenSecretKeyBytes = deriveKey(eigenSecretKey,192);
            eigenSecretKey = new SecretKeySpec(newEigenSecretKeyBytes, 0, newEigenSecretKeyBytes.length, "AES");

        }


    }

    public static void main(String[] args) throws Exception {

        // fire to localhost port 1099
        Registry myRegistry = LocateRegistry.getRegistry("localhost", 1099);

        // search for CommService
        Communicatie impl = (Communicatie) myRegistry.lookup("CommService");

        Client klant = new Client(impl);
        klant.clientBump();

        // Text Area at the Center
        JTextArea ta = new JTextArea();

        Thread t = new Thread(() -> {
            while (true) {
                System.out.println("IN WHILE");
                if (klant.gebumped) {
                    String bericht;
                    try {
                        bericht = klant.clientReceive();
                        System.out.println("BERICHT IN WHILE" +bericht );
                        ta.append("Chat partner: "+bericht);
                        ta.append("\n");
                        System.out.println(bericht);
                    } catch (Exception e) {
                        e.printStackTrace();
                    }

                }
            }
        });

        t.start();

        //GUI
        JFrame frame = new JFrame("Chat Frame");
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setSize(400, 200);

        //Creating the panel at bottom and adding components
        JPanel panel = new JPanel(); // the panel is not visible in output
        JLabel label = new JLabel("Bericht: ");
        JTextField tf = new JTextField(10); // accepts upto 10 characters
        JButton send = new JButton("Send");
        send.addActionListener(e -> {
            String s = tf.getText();
            if (s != null && klant.gebumped){

                    try {
                        System.out.println("BERICHT IN ACTIONLISTENER: "+s);
                        ta.append("ik: "+s);
                        ta.append("\n");
                        klant.clientSend(s);
                    } catch (Exception ex) {
                        ex.printStackTrace();
                    }

                tf.setText("");
            }
        });



        JButton reset = new JButton("Reset");
        panel.add(label); // Components Added using Flow Layout
        panel.add(tf);
        panel.add(send);
        panel.add(reset);
        reset.addActionListener(e -> tf.setText(""));
        //Adding Components to the frame.
        frame.getContentPane().add(BorderLayout.SOUTH, panel);
        //frame.getContentPane().add(BorderLayout.NORTH, mb);
        frame.getContentPane().add(BorderLayout.CENTER, ta);
        frame.setVisible(true);



        //keys:
//        SecretKey Symmetrickey = createAESKey();
//        System.out.println("The Symmetric Key is :" + DatatypeConverter.printHexBinary(Symmetrickey.getEncoded()));
//
//        byte[] initializationVector = createInitializationVector();
//
//        String plainText = "This is the message I want To Encrypt.";
//
//        // Encrypting the message using the symmetric key
//        byte[] cipherText = do_AESEncryption(plainText, Symmetrickey, initializationVector);
//
//        System.out.println("The ciphertext or Encrypted Message is: " + DatatypeConverter.printHexBinary(cipherText));
//
//        // Decrypting the encrypted message
//        String decryptedText = do_AESDecryption(cipherText, Symmetrickey, initializationVector);
//
//        System.out.println("Your original message is: " + decryptedText);
//
//        System.out.println("derived key: ");
//        byte[]derivedkeybyte = deriveKey(Symmetrickey,192);
//        SecretKey derivedKey = new SecretKeySpec(derivedkeybyte, 0, derivedkeybyte.length, "AES");
//        System.out.println(DatatypeConverter.printHexBinary(derivedKey.getEncoded()));
//
//        //Uit te wisselen waarden in key exchange:
//        //symmetrische sleutel
//        SecretKey Kab = Symmetrickey;
//        //random index voor eerste bericht
//        int idxab = random.nextInt(); //bound moet waarschijnlijk = lengte van board
//        //random tag voor bericht voor eerste bericht
//        int tagab = random.nextInt();


    }
}
