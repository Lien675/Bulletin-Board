package Client;

import Interface.Communicatie;

import javax.swing.*;
import java.awt.*;
import java.io.*;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

public class MainClient {
    static Client klant;

    public static void main(String[] args) {
        //GUI
        JFrame frame = new JFrame("Chat Frame");
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setSize(600, 400);


        // Text Area at the Center
        JTextArea ta = new JTextArea();
        //ta.setBounds(20,75,250,200);
        ta.setBackground(Color.DARK_GRAY);
        ta.setForeground(Color.WHITE);
        ta.setColumns(2);
        ta.setEditable(false);


        Thread updateThread = new Thread(() -> {

            Runnable helloRunnable = () -> {
                if (klant.gebumped /*&& klant.isOnline*/) {
                    String bericht;
//                    System.out.println("in runnable");
                    try {
                        bericht = klant.clientReceive();
                        if(bericht!=null){
                            System.out.println("ontvangen bericht in updateThread: "+bericht);
                            ta.append(/*"Chat partner: " + */bericht);
                            ta.append("\n");
                            System.out.println(bericht);
                        }

                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            };
            ScheduledExecutorService executor = Executors.newScheduledThreadPool(1);
            executor.scheduleAtFixedRate(helloRunnable, 0, 1, TimeUnit.SECONDS);

//            while (true) {
//                if (klant.gebumped /*&& klant.isOnline*/) {
//                    String bericht;
//                    System.out.println("in whillllle");
//                    try {
//                        bericht = klant.clientReceive();
//                        System.out.println("ontvangen bericht in updateThread: "+bericht);
//                        ta.append(/*"Chat partner: " + */bericht);
//                        ta.append("\n");
//                        System.out.println(bericht);
//                    } catch (Exception e) {
//                        e.printStackTrace();
//                    }
//                }
//            }
        });

        //Creating the panel at bottom and adding components
        JPanel panel = new JPanel(); // the panel is not visible in output
        JLabel label = new JLabel("Bericht: ");
        JTextField tf = new JTextField(10); // accepts upto 10 characters
        JButton send = new JButton("Send");
        send.addActionListener(e -> {
            String s = tf.getText();
            if (s != null && klant.gebumped) {
                if(klant.aantalTokens>0) {
                    try {
                        ta.append("ik: " + s);
                        ta.append("\n");
                        klant.clientSend(s);
                    } catch (Exception ex) {
                        ex.printStackTrace();
                    }
                    tf.setText("");
                }
                else {
                    ta.append("Automatic Response: Je hebt geen tokens meer om een bericht te verzenden. Ontvang eerst een bericht.");
                    ta.append("\n");
                }

            }
        });
        send.setEnabled(false);

        // Config
        JLabel naamLabel = new JLabel("Gebruikersnaam: ");
        JTextField naamTf = new JTextField(10); // accepts upto 10 characters
        JLabel poortLabel = new JLabel("Poortnummer: ");
        JTextField poortTf = new JTextField(10); // accepts upto 10 characters
        poortTf.setText("30123");
        JButton submit = new JButton("Ok");
        //JButton goOnline = new JButton("Go Online");
        submit.addActionListener(e -> {
            new Thread(() -> {
                try {
                    setParams(naamTf.getText(), poortTf.getText(), submit, send, updateThread);
                } catch (Exception ex) {
                    ex.printStackTrace();
                }
            }).start();

//            try {
//                setParams(naamTf.getText(), poortTf.getText(), submit, send, updateThread/*, goOnline*/);
//
//            } catch (Exception ex) {
//                ex.printStackTrace();
//            }
        });

//        goOnline.addActionListener(e -> {
//            try {
//                if(goOnline.getText().contentEquals("Go Online")){
//                    klant.isOnline = true;
//                    ta.append("Je bent online. Veel chat plezier!");
//                    ta.append("\n");
//                    goOnline.setEnabled(false);
//                        updateThread.start();
//                        Thread tempThread= updateThread;
//                }
//
//
//            } catch (Exception ex) {
//                ex.printStackTrace();
//            }
//        });

        submit.setEnabled(true);
        JPanel configPanel = new JPanel();
        configPanel.add(naamLabel);
        configPanel.add(naamTf);
        configPanel.add(poortLabel);
        configPanel.add(poortTf);
        configPanel.add(submit);

//        goOnline.setEnabled(false);
//        configPanel.add(goOnline);

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
        frame.getContentPane().add(BorderLayout.NORTH, configPanel);
        frame.setVisible(true);
        clear.addActionListener(e -> ta.setText(""));

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

    private static void setParams(String naam, String poorttekst, JButton submit, JButton send, Thread updateThread/*, JButton goOnline*/) throws Exception {
        int poort;
        if (naam.isEmpty() || naam.isBlank()) return;
        try {
            poort = Integer.parseInt(poorttekst);

            // fire to localhost port 1099
            Registry myRegistry = LocateRegistry.getRegistry("localhost", 1099);
            // search for CommService
            Communicatie commImpl = (Communicatie) myRegistry.lookup("CommService");



//            goOnline.setEnabled(true);

            klant = new Client(naam, poort, commImpl);
            System.out.println("KLANT MET NAAM:"+naam);

            if(!klant.gebumped){
                System.out.println("in if van is client gebumped");

                klant.clientBump();
            }

            submit.setEnabled(false);
            send.setEnabled(true);

            updateThread.start();
        } catch (NumberFormatException ignored) {
        }
    }

}
