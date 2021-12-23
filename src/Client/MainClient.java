package Client;

import Interface.Communicatie;

import javax.swing.*;
import java.awt.*;
import java.rmi.NotBoundException;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;

public class MainClient {

    public static void main(String[] args) throws RemoteException, NotBoundException {

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
        Thread t = new Thread(new Runnable() {
            @Override
            public void run() {
                while (true) {
                    System.out.println("in while");
                    if (wrapper.gebruiker != null) {
                        System.out.println("in if");
                        String bericht = null;
                        try {
                            bericht = impl.ontvangBericht(wrapper.gebruiker);
                        } catch (RemoteException e) {
                            e.printStackTrace();
                        }
                        ta.append(bericht);
                    }
                }
            }
        });
        t.start();

    }
}
