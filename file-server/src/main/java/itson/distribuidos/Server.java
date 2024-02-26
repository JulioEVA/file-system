package itson.distribuidos;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.SocketException;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.io.FileOutputStream;

public class Server {
    public static void main(String[] args) {
        final int PORT = 5000;
        byte[] buffer = new byte[1024];

        try {
            while (true) {
                DatagramSocket udpSocket = new DatagramSocket(PORT);
                System.out.println("UDP Server started at port " + PORT);

                // Receive file from client
                DatagramPacket request = new DatagramPacket(buffer, buffer.length);
                udpSocket.receive(request);
                System.out.println("Received file from client");

                // Save file to disk
                FileOutputStream fos = new FileOutputStream("src/main/resources/lorem-received.txt");
                fos.write(request.getData());
                fos.close();
                System.out.println("File saved to disk");

                // Send response to client
                String responseMessage = "File received";
                buffer = responseMessage.getBytes();
                DatagramPacket response = new DatagramPacket(buffer, buffer.length,
                        request.getAddress(), request.getPort());
                udpSocket.send(response);
                System.out.println("Response sent to client");

                udpSocket.close();
            }
        } catch (SocketException e) {
            Logger.getLogger(Server.class.getName()).log(Level.SEVERE, null, e);
        } catch (IOException e) {
            Logger.getLogger(Server.class.getName()).log(Level.SEVERE, null, e);
        } finally {
            System.out.println("Server closed");
        }
    }
}
