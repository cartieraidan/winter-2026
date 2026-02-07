import java.io.*;
import java.net.*;

public class Client {
    private String  playerName;
    private int playerID;

    DatagramPacket sendPacket, receivePacket;
    DatagramSocket sendReceiveSocket;

    public Client() {
        try {
            System.out.println("Client started. Socket on random port.\n" + "Enter your player name: ");
            BufferedReader input = new BufferedReader(new InputStreamReader(System.in));
            this.playerName = input.readLine();

        } catch (IOException e) {
            System.out.println("Could not read player name");
            System.exit(1);
        }

        try {
            sendReceiveSocket = new DatagramSocket();

        } catch (SocketException e) {
            System.out.println("Socket Exception at Constructor of client");
            System.exit(1);
        }
    }

    public String getPlayerName() {
        return playerName;
    }

    public void send() { // !!!!!! add params to accept from client / maybe change this to sendToServer

        String message = "@Hello from client";

        // convert string into bytes array
        byte[] msg = message.getBytes();

        // make datagram packet to be sent to the server
        try {
            sendPacket = new DatagramPacket(msg, msg.length, InetAddress.getLocalHost(), 5000);

        } catch (UnknownHostException uhe) {
            System.out.println("Unknown Host Exception");
            System.exit(1);
        }

        System.out.println("Client: sent: ");
        System.out.println("To host: " + sendPacket.getAddress());
        System.out.println("To host port: " + sendPacket.getPort());
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

        this.receive();
    }

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

        // process received packet
        System.out.println("Client: received: ");
        System.out.println("From host: " + receivePacket.getAddress());
        System.out.println("From host port: " + receivePacket.getPort());
        int len = receivePacket.getLength();
        System.out.println("Length: " + len);
        System.out.print("Containing: ");

        // Form a String from the byte array.
        String received = new String(data,0,len);
        System.out.println(received);

        // We're finished, so close the socket.
        sendReceiveSocket.close(); // !!!!!modify after

    }

    public static void main( String[] args ) {
        Client client = new Client();
        client.send();
    }

}
