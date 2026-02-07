import java.io.*;
import java.net.*;

public class Host {
    DatagramPacket sendPacket, receivePacket;
    DatagramSocket sendReceiveSocket; // !!!!!!! might add separate sockets / create receive/send for client and server
    // so sendReceiveServer & sendReceiveClient & maybe for packets as well

    public Host() {
        try {
            sendReceiveSocket = new DatagramSocket(); // !!!!!! will need a similar one like server to receive from client

            System.out.println("Battle Royal Server started on port 6000");
        } catch (SocketException e) {
            System.out.println("Socket Exception at Constructor of server");
            System.exit(1);
        }
    }

    public void send() { // !!!!!! add params to accept from client / maybe change this to sendToServer

        String message = "Hello connection made";

        // convert string into bytes array
        byte[] msg = message.getBytes();

        // make datagram packet to be sent to the server
        try {
            sendPacket = new DatagramPacket(msg, msg.length, InetAddress.getLocalHost(), 6000);

        } catch (UnknownHostException uhe) {
            System.out.println("Unknown Host Exception");
            System.exit(1);
        }

        System.out.println("Host: forwarded: ");
        System.out.println("To server: " + sendPacket.getAddress());
        System.out.println("To server port: " + sendPacket.getPort());
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
        System.out.println("Host: received: ");
        System.out.println("From server: " + receivePacket.getAddress());
        System.out.println("From server port: " + receivePacket.getPort());
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
        Host host = new Host();
        host.send();
    }
}
