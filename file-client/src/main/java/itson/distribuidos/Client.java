package itson.distribuidos;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.SocketException;
import java.net.UnknownHostException;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.io.File;
import java.io.FileInputStream;

public class Client {
    public static void main(String[] args) {
        final int SERVER_PORT = 5000;
        byte[] buffer = new byte[1024];

        try {

            InetAddress serverAddress = InetAddress.getByName("localhost");
            DatagramSocket udpSocket = new DatagramSocket();

            // Read file into byte array
            File file = new File("src/main/resources/lorem.txt");
            FileInputStream fis = new FileInputStream(file);
            fis.read(buffer);
            fis.close();

            // Send file to server
            System.out.println("Sending file to server");
            DatagramPacket request = new DatagramPacket(buffer, buffer.length, serverAddress, SERVER_PORT);
            udpSocket.send(request);

            // Receive response from server
            DatagramPacket response = new DatagramPacket(buffer, buffer.length);
            System.out.println("Waiting for response");
            udpSocket.receive(response);
            System.out.println("Received message from server:");
            System.out.println(response.getData());

            udpSocket.close();

        } catch (SocketException e) {
            Logger.getLogger(Client.class.getName()).log(Level.SEVERE, null, e);
        } catch (UnknownHostException e) {
            Logger.getLogger(Client.class.getName()).log(Level.SEVERE, null, e);
        } catch (IOException e) {
            Logger.getLogger(Client.class.getName()).log(Level.SEVERE, null, e);
        }
    }
}