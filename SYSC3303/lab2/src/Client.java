import java.io.*;
import java.net.*;

/**
 * Class made to represent Client playing game.
 *
 * @author Aidan Cartier
 * @version Feb 7, 2026
 */
public class Client {
    private String  playerName;
    private int playerID;

    DatagramPacket sendPacket, receivePacket;
    DatagramSocket sendReceiveSocket;

    /**
     * Constructor creating socket for client.
     */
    public Client() {

        try {
            sendReceiveSocket = new DatagramSocket(); // create socket

        } catch (SocketException e) {
            System.out.println("Socket Exception at Constructor of client");
            System.exit(1);
        }
    }

    /**
     * Get player name.
     *
     * @return String of player name.
     */
    public String getPlayerName() {
        return playerName;
    }

    /**
     * Method send, sends a datagram using UDP to Host.
     *
     * @param message String messaging containing to send.
     */
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

    /**
     * Method for receives datagram from host.
     */
    public void receive() {

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


    }

    /**
     * Method process to process string messaged received from host.
     *
     * @param received String to determine next state.
     * @return Return a returning message to send back after player input.
     */
    private String process(String received) {
        if (received.equals("QUITED")) { // if quit processed from server
            System.out.println("Client quit");
            System.exit(1);
        }

        String message = "@"; // used to identify from where the message is coming from

        String command; // index 0
        String playerDt; // index 1 -> player name or player id

        // messages from server that do not require any other logic
        if (received.startsWith("PLAYERS=") || received.equals("MOVE_OK")
                || received.equals("BAD_INPUT") || received.equals("PICKUP_OK") || received.equals("PICKUP_FAIL")) {
            message = message + this.playerCommand();
            command = "STATE";
            playerDt = "returnOptions";
        } else { // everything else
            String[] output = received.split(":", 2); // split message

            if (output.length > 1) {
                command = output[0];
                playerDt = output[1];
            } else {
                return message + "error"; // receive a bad input
            }
        }

        if (command.equals("JOINED")) { // for when player first joins
            this.playerID = Integer.parseInt(playerDt);
            System.out.println("Joined game with playerId = " + this.playerID);

            message = message + this.playerCommand();
        }

        return message;
    }

    /**
     * Method handles most all states for Client and determining what the client shall send to server.
     *
     * @return String for next message.
     */
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

    /**
     * Method is for getting player name and ID.r
     */
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
