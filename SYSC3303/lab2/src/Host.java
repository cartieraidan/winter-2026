import java.io.*;
import java.net.*;

/**
 * Class acting as the relay between Client and Server.
 *
 * @author Aidan Cartier
 * @version Feb 7, 2026
 */
public class Host {
    DatagramPacket sendPacket, receivePacket;
    DatagramSocket sendReceiveSocket;

    int previousClientPort = 0; // so we can send the message back to client
    public enum FROM {server, client}; // track who is sending message
    FROM from = FROM.client;

    /**
     * Constructor for initializing socket.
     */
    public Host() {
        try {
            sendReceiveSocket = new DatagramSocket(5000);

            System.out.println("Battle Royal Server started on port 6000");
        } catch (SocketException e) {
            System.out.println("Socket Exception at Constructor of host");
            System.exit(1);
        }
    }

    /**
     * Method for forwarding messages to Client and Server.
     *
     * @param message String message sending
     */
    public void forward(String message) { // !!!!!! add params to accept from client / maybe change this to sendToServer

        //String message = "Hello connection made";

        int forwardPort;

        // convert string into bytes array
        byte[] msg = message.getBytes();

        // make datagram packet to be sent to the server
        try {
            if (from == FROM.client && previousClientPort != 0) {
                forwardPort = previousClientPort; // returning message from server to client
            } else {
                forwardPort = 6000; // server port
            }
            sendPacket = new DatagramPacket(msg, msg.length, InetAddress.getLocalHost(), forwardPort);

        } catch (UnknownHostException uhe) {
            System.out.println("Unknown Host Exception");
            System.exit(1);
        }

        System.out.println("Host: forwarded: ");
        System.out.println("To " + from + ": " + sendPacket.getAddress());
        System.out.println("To " + from + " port: " + sendPacket.getPort());
        int len = sendPacket.getLength();
        System.out.println("Length: " + len);
        System.out.print("Containing: ");
        System.out.println(new String(sendPacket.getData(),0,len) + "\n");

        // send packet
        try {
            sendReceiveSocket.send(sendPacket);

        } catch (IOException e) {
            System.out.println("Send Socket Timed Out. Host\n" + e);
            System.exit(1);
        }

        if (message.equals("QUITED")) {
            System.out.println("Host quit");
            System.exit(1);
        }

        this.receive();
    }

    /**
     * Method for receiving messages from Client and Server.
     */
    public void receive() { // maybe have separate methods for server and client?

        // creating datagram packet to receive bytes
        byte[] data = new byte[100];
        receivePacket = new DatagramPacket(data, data.length);

        // wait until receive packet
        try {
            sendReceiveSocket.receive(receivePacket);

        } catch (IOException e) {
            System.out.println("Receive Socket Timed Out. Host\n" + e);
            System.exit(1);
        }

        int len = receivePacket.getLength();
        // Form a String from the byte array.
        String received = new String(data,1,len - 1);

        String _char = new String(data, 0, 1);
        if (_char.equals("@")) {
            //System.out.println("Host received @ -> client");

            from = FROM.client;
            previousClientPort = receivePacket.getPort();

        } else if (_char.equals("#")) {
            //System.out.println("Host received # -> server");

            from = FROM.server;

        }



        // process received packet
        System.out.println("Host: received: ");
        System.out.println("From " + from + ": " + receivePacket.getAddress());
        System.out.println("From " + from + " port: " + receivePacket.getPort());
        //int len = receivePacket.getLength();
        System.out.println("Length: " + len);
        System.out.print("Containing: ");

        // Form a String from the byte array.
        //String received = new String(data,0,len);
        System.out.println(received + "\n");

        // We're finished, so close the socket.
        //sendReceiveSocket.close(); // !!!!!modify after


        from = (from == FROM.client) ? FROM.server : FROM.client; // reversing since get from client then forwarding to server
        this.forward(received);


    }

    public static void main( String[] args ) {
        Host host = new Host();
        host.receive();
    }
}
