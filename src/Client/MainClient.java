package Client;

import Interface.Communicatie;

import javax.swing.*;
import java.awt.*;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;

public class MainClient {

    public static void main(String[] args) throws Exception {
        // fire to localhost port 1099
        Registry myRegistry = LocateRegistry.getRegistry("localhost", 1099);

        // search for CommService
        Communicatie impl = (Communicatie) myRegistry.lookup("CommService");

        Client klant = new Client(impl);
        klant.clientBump();

        // Text Area at the Center
        JTextArea ta = new JTextArea();
        //ta.setBounds(20,75,250,200);
        ta.setBackground(Color.DARK_GRAY);
        ta.setForeground(Color.WHITE);
        ta.setColumns(2);
        ta.setEditable(false);

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
            if (s != null && klant.gebumped) {
                try {
                    ta.append("ik: " + s);
                    ta.append("\n");
                    klant.clientSend(s);
                } catch (Exception ex) {
                    ex.printStackTrace();
                }
                tf.setText("");
            }
        });

        JButton reset = new JButton("Reset");
        JButton clear = new JButton("Clear");
        panel.add(label); // Components Added using Flow Layout
        panel.add(tf);
        panel.add(send);
        panel.add(reset);
        panel.add(clear);
        reset.addActionListener(e -> tf.setText(""));
        //Adding Components to the frame.
        frame.getContentPane().add(BorderLayout.SOUTH, panel);
        frame.getContentPane().add(BorderLayout.CENTER, ta);
        frame.setVisible(true);
        clear.addActionListener(e -> ta.setText(""));

        Thread t = new Thread(() -> {
            while (true) {
                if (klant.gebumped) {
                    String bericht;
                    try {
                        bericht = klant.clientReceive();
                        ta.append("Chat partner: " + bericht);
                        ta.append("\n");
                        System.out.println(bericht);
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            }
        });
        t.start();

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
