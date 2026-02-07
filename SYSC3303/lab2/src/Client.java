import java.io.*;
import java.net.*;

public class Client {
    private String  playerName;
    private int playerID;

    DatagramPacket sendPacket, receivePacket;
    DatagramSocket sendReceiveSocket;

    public enum Action {
        JOIN
    }

    public Client() {

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

    public void send(String message) {

        //String message = "@Hello from client";

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

        String message = this.process(received);

        this.send(message);
        // We're finished, so close the socket.
        //sendReceiveSocket.close(); // !!!!!modify after


    }

    private String process(String received) {
        if (received.equals("QUITED")) {
            System.out.println("Client quit");
            System.exit(1);
        }

        String message = "@";

        String command;
        String action;

        if (received.startsWith("PLAYERS=") || received.equals("MOVE_OK") || received.equals("BAD_INPUT")) {
            message = message + this.playerCommand();
            command = "STATE";
            action = "returnOptions";
        } else {
            String[] output = received.split(":", 2);


            if (output.length > 1) {
                command = output[0];
                action = output[1];
            } else {
                return message + "error";
            }
        }

        if (command.equals("JOINED")) {
            this.playerID = Integer.parseInt(action);
            System.out.println("Joined game with playerId = " + this.playerID);

            message = message + this.playerCommand();
        }

        return message;
    }

    private String playerCommand() {
        String message = "";

        System.out.println("Commands: MOVE dx dy | PICKUP lootId | STATE | QUIT");

        try {
            BufferedReader input = new BufferedReader(new InputStreamReader(System.in));
            String[] consoleOutput = input.readLine().split(" ", 3);

            if (consoleOutput.length == 3) {
                message = message + "MOVE:" + this.playerID + ":" + consoleOutput[1] + ":" + consoleOutput[2];
            } else if (consoleOutput.length == 2) {
                message = message + "PICKUP:" + this.playerID + ":" + consoleOutput[1];
            } else if (consoleOutput.length == 1) {
                if (consoleOutput[0].equals("STATE")) {
                    message = message + "STATE:" + this.playerID;
                } else if  (consoleOutput[0].equals("QUIT")) {
                    message = message + "QUIT:" + this.playerID;
                } else { // catch all
                    return this.playerCommand();
                }
            }

        } catch (IOException e) {
            System.out.println("Error while reading console output");
            System.exit(1);
        }

        System.out.println("\n");

        return message;
    }

    public void join() {
        try {
            System.out.println("Client started. Socket on random port.\n" + "Enter your player name: ");
            BufferedReader input = new BufferedReader(new InputStreamReader(System.in));
            this.playerName = input.readLine();

        } catch (IOException e) {
            System.out.println("Could not read player name");
            System.exit(1);
        }

        this.send("@JOIN:" + this.getPlayerName());
    }

    public static void main( String[] args ) {
        Client client = new Client();
        client.join();
    }

}
