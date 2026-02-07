import java.io.*;
import java.net.*;

public class Server {

    DatagramPacket sendPacket, receivePacket;
    DatagramSocket sendSocket, receiveSocket;

    public Server() {

        try {
            // bind to available port for sending Datagram packets
            sendSocket = new DatagramSocket();

            // bind socket to port 6000 for receiving messages
            receiveSocket = new DatagramSocket(6000);

            System.out.println("Battle Royal Server started on port 6000");

        } catch (SocketException e) {
            System.out.println("Socket Exception at Constructor of server");
            System.exit(1);
        }

        // !!!!! need to initialize gameState here
    }

    public void receive() {
        // construct datagram packet for receiving up to 100 bytes
        byte[] data = new byte[100];
        receivePacket = new DatagramPacket(data, data.length);

        // wait until receive a packet
        try {
            receiveSocket.receive(receivePacket);

        } catch (IOException e) {
            System.out.println("Receive Socket Timed Out. Server\n" + e);
            System.exit(1);
        }

        // process received datagram
        System.out.println("Server: received:");
        System.out.println("From host: " + receivePacket.getAddress());
        System.out.println("From host port: " + receivePacket.getPort());
        int length = receivePacket.getLength();
        System.out.println("Length: " + length);
        System.out.print("Containing: ");

        // get string from byte array
        String received = new String(data, 0, length);
        System.out.println(received + "\n");

        // Adding delay
        try {
            Thread.sleep(2000);
        } catch (InterruptedException e ) {
            System.out.println("Sleep interrupted: " + e);
            System.exit(1);
        }

        // !!!!! message processed
        String message = "#Connected";

        // convert string into bytes array
        byte[] data2 = new byte[100];
        data2 = message.getBytes();
        this.echo(data2); // !!!!!!! here should be where we process the command and if it was valid to move

    }

    public void echo(byte[] data) {
        // creating datagram packet to send back to host
        sendPacket = new DatagramPacket(data, data.length, receivePacket.getAddress(), receivePacket.getPort());

        System.out.println("Server: sent: ");
        System.out.println("To host: " + sendPacket.getAddress());
        System.out.println("To host port: " + sendPacket.getPort());
        int len = sendPacket.getLength();
        System.out.println("Length: " + len);
        System.out.print("Containing: ");
        System.out.println(new String(sendPacket.getData(),0,len) + "\n"); // !!!!!!! here we're sending back the message
        // !!!!!!!! but we should be sending a validation if it was valid or not

        // send datagram packet back to host
        try {
            sendSocket.send(sendPacket);
        }  catch (IOException e) {
            System.out.println("Send Socket Timed Out. Server\n" + e);
            System.exit(1);
        }
    }

    public static void main( String[] args ) {
        Server server = new Server();
        server.receive();
    }
}
