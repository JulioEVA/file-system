package itson.distribuidos;

import java.util.ArrayList;
import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseListener;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;

public class Server {

    static ArrayList<MyFile> files = new ArrayList<>();
    static int fileId = 0;

    public static void main(String[] args) {
        final int PORT = 5000;

        JFrame jFrame = new JFrame("File Server");
        jFrame.setSize(450, 450);
        jFrame.setLayout(new BoxLayout(jFrame.getContentPane(), BoxLayout.Y_AXIS));
        jFrame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

        JPanel jPanel = new JPanel();
        jPanel.setLayout(new BoxLayout(jPanel, BoxLayout.Y_AXIS));

        JScrollPane jScrollPanel = new JScrollPane(jPanel);
        jScrollPanel.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_ALWAYS);

        JLabel jlTitle = new JLabel("File receiver");
        jlTitle.setFont(new Font("Arial", Font.BOLD, 25));
        jlTitle.setBorder(new EmptyBorder(20, 0, 10, 0));
        jlTitle.setAlignmentX(Component.CENTER_ALIGNMENT);

        jFrame.add(jlTitle);
        jFrame.add(jScrollPanel);
        jFrame.setVisible(true);

        try (DatagramSocket udpSocket = new DatagramSocket(PORT)) {

            int bufferSize = 1024;
            byte[] buffer = new byte[bufferSize];

            while (true) {
                // Receive packet containing file name
                DatagramPacket fileNamePacket = new DatagramPacket(buffer, buffer.length);
                udpSocket.receive(fileNamePacket);
                String fileName = new String(fileNamePacket.getData(), 0, fileNamePacket.getLength());

                // Create FileOutputStream to write file content
                FileOutputStream fileOutputStream = new FileOutputStream(fileName);

                ByteArrayOutputStream byteStream = new ByteArrayOutputStream();

                // Receive and write file content in chunks
                while (true) {
                    DatagramPacket contentPacket = new DatagramPacket(buffer, buffer.length);
                    udpSocket.receive(contentPacket);
                    int bytesRead = contentPacket.getLength();
                    if (bytesRead <= 0) {
                        break; // End of file
                    }
                    fileOutputStream.write(contentPacket.getData(), 0, bytesRead);
                    byteStream.write(contentPacket.getData(), 0, bytesRead);
                }

                // Get the byte array of the file content
                byte[] fileContentBytes = byteStream.toByteArray();
                fileOutputStream.close();
                byteStream.close();

                // Add file to list
                MyFile newFile = new MyFile(fileId, fileName, fileContentBytes, getFileExtension(fileName));
                files.add(newFile);

                SwingUtilities.invokeLater(() -> {
                    JPanel jpFileRow = new JPanel();
                    jpFileRow.setLayout(new BoxLayout(jpFileRow, BoxLayout.X_AXIS));

                    JLabel jlFileName = new JLabel(fileName);
                    jlFileName.setFont(new Font("Arial", Font.BOLD, 15));
                    jlFileName.setBorder(new EmptyBorder(10, 0, 10, 0));

                    if (getFileExtension(fileName).equalsIgnoreCase("txt")) {
                        jpFileRow.setName(String.valueOf(fileId));
                        jpFileRow.addMouseListener(getMyMouseListener());

                        jpFileRow.add(jlFileName);
                    } else {
                        jpFileRow.setName(String.valueOf(fileId));
                        jpFileRow.addMouseListener(getMyMouseListener());

                        jpFileRow.add(jlFileName);
                        jPanel.add(jpFileRow);

                        jFrame.validate();
                    }
                });

                fileId++;

            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static String getFileExtension(String fileName) {
        // Doesn't work with files with multiple extension
        int i = fileName.lastIndexOf('.');
        if (i > 0) {
            return fileName.substring(i + 1);
        } else {
            return "";
        }
    }

    public static MouseListener getMyMouseListener() {
        return new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                JPanel jPanel = (JPanel) evt.getSource();
                int fileId = Integer.parseInt(jPanel.getName());

                for (MyFile file : files) {
                    if (file.getId() == fileId) {
                        JFrame jfPrview = createFrame(file.getName(), file.getData(), file.getFileExtension());
                        jfPrview.setVisible(true);
                    }
                }

            }
        };
    }

    public static JFrame createFrame(String fileName, byte[] fileData, String fileExtension) {
        JFrame jframe = new JFrame("File Downloader");
        jframe.setSize(450, 450);

        JPanel jPanel = new JPanel();
        jPanel.setLayout(new BoxLayout(jPanel, BoxLayout.Y_AXIS));

        JLabel jlTitle = new JLabel("File Preview");
        jlTitle.setFont(new Font("Arial", Font.BOLD, 25));
        jlTitle.setBorder(new EmptyBorder(20, 0, 10, 0));
        jlTitle.setAlignmentX(Component.CENTER_ALIGNMENT);

        JLabel jlPrompt = new JLabel("Are you sure you want to download " + fileName + "?");
        jlPrompt.setFont(new Font("Arial", Font.BOLD, 15));
        jlPrompt.setBorder(new EmptyBorder(20, 0, 10, 0));
        jlPrompt.setAlignmentX(Component.CENTER_ALIGNMENT);

        JButton jbDownload = new JButton("Download");
        jbDownload.setPreferredSize(new Dimension(150, 75));
        jbDownload.setFont(new Font("Arial", Font.BOLD, 20));

        JButton jbCancel = new JButton("Cancel");
        jbCancel.setPreferredSize(new Dimension(150, 75));
        jbCancel.setFont(new Font("Arial", Font.BOLD, 20));

        JLabel jlFileContent = new JLabel();
        jlFileContent.setFont(new Font("Arial", Font.BOLD, 15));
        jlFileContent.setAlignmentX(Component.CENTER_ALIGNMENT);

        JPanel jpButtons = new JPanel();
        jpButtons.setBorder(new EmptyBorder(20, 0, 0, 0));
        jpButtons.add(jbDownload);
        jpButtons.add(jbCancel);

        if (fileExtension.equalsIgnoreCase("txt")) {
            jlFileContent.setText("<html>" + new String(fileData) + "</html>");
        } else {
            jlFileContent.setIcon(new ImageIcon(fileData));
        }

        jbDownload.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                File fileToDownload = new File(fileName);

                try {
                    FileOutputStream fileOutputStream = new FileOutputStream(fileToDownload);
                    fileOutputStream.write(fileData);
                    fileOutputStream.close();

                    jframe.dispose();
                } catch (IOException ex) {
                    ex.printStackTrace();
                }
            }
        });

        jbCancel.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                jframe.dispose();
            }
        });

        jPanel.add(jlTitle);
        jPanel.add(jlPrompt);
        jPanel.add(jlFileContent);
        jPanel.add(jpButtons);

        jframe.add(jPanel);

        return jframe;
    }

}
