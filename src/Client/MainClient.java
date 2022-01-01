package Client;

import Interface.Communicatie;

import javax.swing.*;
import java.awt.*;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
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
                if (klant.gebumped) {
                    String bericht;
                    try {
                        bericht = klant.clientReceive();
                        if (bericht != null) {
                            ta.append(bericht);
                            ta.append("\n");
                        }
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            };
            ScheduledExecutorService executor = Executors.newScheduledThreadPool(1);
            executor.scheduleAtFixedRate(helloRunnable, 0, 100, TimeUnit.MILLISECONDS);
        });

        //Creating the panel at bottom and adding components
        JPanel panel = new JPanel(); // the panel is not visible in output
        JLabel label = new JLabel("Bericht: ");
        JTextField tf = new JTextField(10); // accepts upto 10 characters
        JButton send = new JButton("Send");
        send.addActionListener(e -> {
            String s = tf.getText();
            if (s != null && klant.gebumped) {
                if (klant.aantalTokens > 0) {
                    try {
                        ta.append("ik: " + s);
                        ta.append("\n");
                        klant.clientSend(s);
                    } catch (Exception ex) {
                        ex.printStackTrace();
                    }
                    tf.setText("");
                } else {
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
        submit.addActionListener(e -> new Thread(() -> {
            try {
                setParams(naamTf.getText(), poortTf.getText(), submit, send, updateThread);
            } catch (Exception ex) {
                ex.printStackTrace();
            }
        }).start());

        submit.setEnabled(true);
        JPanel configPanel = new JPanel();
        configPanel.add(naamLabel);
        configPanel.add(naamTf);
        configPanel.add(poortLabel);
        configPanel.add(poortTf);
        configPanel.add(submit);

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

            klant = new Client(naam, poort, commImpl);
            if (!klant.gebumped) {
                klant.clientBump();
            }

            submit.setEnabled(false);
            send.setEnabled(true);

            updateThread.start();
        } catch (NumberFormatException ignored) {
        }
    }

}
