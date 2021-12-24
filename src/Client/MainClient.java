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
import java.rmi.NotBoundException;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.KeySpec;
import java.util.Base64;
import java.util.Random;

public class MainClient {


    public static final String AES = "AES";
    // We are using a Block cipher(CBC mode)
    private static final String AES_CIPHER_ALGORITHM = "AES/CBC/PKCS5PADDING";
    static Random random = new Random(4);
    public int indexab;
    public int tagab;
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
        return key;
    }

    // Function to initialize a vector with an arbitrary value
    public static byte[] createInitializationVector()
    {

        // Used with encryption
        byte[] initializationVector
                = new byte[16];
        SecureRandom secureRandom
                = new SecureRandom();
        secureRandom.nextBytes(initializationVector);
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

    public static void send(int index, int tag, String message, SecretKey Kab) throws Exception {
        //random index voor volgende bericht
        int idxab = random.nextInt(); //bound moet waarschijnlijk = lengte van board
        //random tag voor bericht voor volgende bericht
        int tagab = random.nextInt();

        String allTogether = message + Integer.toString(idxab) + Integer.toString(tagab);
        byte[] initializationVector = createInitializationVector();
        byte[] cipherText = do_AESEncryption(allTogether, Kab, initializationVector);

        //hash tag:
        MessageDigest digest = MessageDigest.getInstance("SHA-256");
        byte[] hashedTag = digest.digest(Integer.toString(tag).getBytes());

        write(index,cipherText,hashedTag);

        //this.indexab = idxab
        //this.tagab = tagab
        //Kab = deriveKey(Kab)
    }

    public static void write(int index, byte[]encryptedMessage , byte[] hashedTag){

    }





    public static void main(String[] args) throws Exception {

        var wrapper = new Object(){ String gebruiker = null; };

        // fire to localhost port 1099
        Registry myRegistry = LocateRegistry.getRegistry("localhost", 1099);

        // search for CommService
        Communicatie impl = (Communicatie) myRegistry.lookup("CommService");

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
            if (s != null){
                if (wrapper.gebruiker == null) {
                    wrapper.gebruiker = s;

                    System.out.println(wrapper.gebruiker);
                    try {
                        impl.voegGebruikerToe(wrapper.gebruiker);
                    } catch (RemoteException ex) {
                        ex.printStackTrace();
                    }
                } else {
                    try {
                        impl.stuurBericht(s, wrapper.gebruiker);
                    } catch (RemoteException ex) {
                        ex.printStackTrace();
                    }
                }

                tf.setText("");
            }
        });

        // Text Area at the Center
        JTextArea ta = new JTextArea();

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


        // Display incomming
                while (true) {
                    System.out.println("in while");
                   if (wrapper.gebruiker != null) {
                        System.out.println("in if");
                        String bericht = null;
                        System.out.println("hier geraakt");
                        try {
                            bericht = impl.ontvangBericht(wrapper.gebruiker);
                            System.out.println(bericht);
                        } catch (RemoteException e) {
                            e.printStackTrace();
                        }
                        ta.append(bericht);
                    }
                }


        //keys:
        SecretKey Symmetrickey = createAESKey();
        System.out.println("The Symmetric Key is :" + DatatypeConverter.printHexBinary(Symmetrickey.getEncoded()));

        byte[] initializationVector = createInitializationVector();

        String plainText = "This is the message I want To Encrypt.";

        // Encrypting the message using the symmetric key
        byte[] cipherText = do_AESEncryption(plainText, Symmetrickey, initializationVector);

        System.out.println("The ciphertext or Encrypted Message is: " + DatatypeConverter.printHexBinary(cipherText));

        // Decrypting the encrypted message
        String decryptedText = do_AESDecryption(cipherText, Symmetrickey, initializationVector);

        System.out.println("Your original message is: " + decryptedText);

        System.out.println("derived key: ");
        byte[]derivedkeybyte = deriveKey(Symmetrickey,192);
        SecretKey derivedKey = new SecretKeySpec(derivedkeybyte, 0, derivedkeybyte.length, "AES");
        System.out.println(DatatypeConverter.printHexBinary(derivedKey.getEncoded()));

        //Uit te wisselen waarden in key exchange:
        //symmetrische sleutel
        SecretKey Kab = Symmetrickey;
        //random index voor eerste bericht
        int idxab = random.nextInt(); //bound moet waarschijnlijk = lengte van board
        //random tag voor bericht voor eerste bericht
        int tagab = random.nextInt();


    }
}
