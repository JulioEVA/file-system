package itson.distribuidos;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

public class Client {
    public static void main(String[] args) {
        File[] fileToSend = new File[1];

        JFrame jFrame = new JFrame("File Client");
        jFrame.setSize(450, 450);
        jFrame.setLayout(new BoxLayout(jFrame.getContentPane(), BoxLayout.Y_AXIS));
        jFrame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

        JLabel jlTItle = new JLabel("File Sender");
        jlTItle.setFont(new Font("Arial", Font.BOLD, 25));
        jlTItle.setBorder(new EmptyBorder(20, 0, 10, 0));
        jlTItle.setAlignmentX(Component.CENTER_ALIGNMENT);

        JLabel jlFileName = new JLabel("File to send:");
        jlFileName.setFont(new Font("Arial", Font.BOLD, 15));
        jlFileName.setBorder(new EmptyBorder(50, 0, 10, 0));
        jlFileName.setAlignmentX(Component.CENTER_ALIGNMENT);

        JPanel jpButton = new JPanel();
        jpButton.setBorder(new EmptyBorder(75, 0, 0, 0));

        JButton jbSendFile = new JButton("Send file");
        jbSendFile.setPreferredSize(new Dimension(150, 75));
        jbSendFile.setFont(new Font("Arial", Font.BOLD, 20));

        JButton jbChooseFile = new JButton("Select file");
        jbChooseFile.setPreferredSize(new Dimension(150, 75));
        jbChooseFile.setFont(new Font("Arial", Font.BOLD, 20));

        jpButton.add(jbSendFile);
        jpButton.add(jbChooseFile);

        jbChooseFile.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                JFileChooser jFileChooser = new JFileChooser();
                jFileChooser.setDialogTitle("Select a file to send");
                if (jFileChooser.showOpenDialog(null) == JFileChooser.APPROVE_OPTION) {
                    fileToSend[0] = jFileChooser.getSelectedFile();
                    jlFileName.setText("File to send: " + fileToSend[0].getName());
                }
            }
        });

        jbSendFile.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                if (fileToSend[0] != null) {
                    try (FileInputStream fileInputStream = new FileInputStream(fileToSend[0].getAbsolutePath())) {
                        InetAddress serverAddress = InetAddress.getByName("localhost");
                        int serverPort = 5000;

                        int bufferSize = 1024;
                        byte[] buffer = new byte[bufferSize];

                        try (DatagramSocket udpSocket = new DatagramSocket()) {
                            // Read file name
                            String fileName = fileToSend[0].getName();
                            byte[] fileNameBytes = fileName.getBytes();

                            // Create and send packet containing file name
                            DatagramPacket fileNamePacket = new DatagramPacket(fileNameBytes, fileNameBytes.length,
                                    serverAddress, serverPort);
                            udpSocket.send(fileNamePacket);

                            // Read file content and send in chunks
                            int bytesRead;
                            while ((bytesRead = fileInputStream.read(buffer)) != -1) {
                                DatagramPacket contentPacket = new DatagramPacket(buffer, bytesRead, serverAddress,
                                        serverPort);
                                udpSocket.send(contentPacket);
                            }
                        }
                    } catch (IOException e1) {
                        e1.printStackTrace();
                    }
                } else {
                    jlFileName.setText("Please select a file to send");
                }
            }
        });

        jFrame.add(jlTItle);
        jFrame.add(jlFileName);
        jFrame.add(jpButton);
        jFrame.setVisible(true);
    }
}