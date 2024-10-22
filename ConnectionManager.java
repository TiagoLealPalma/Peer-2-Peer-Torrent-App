import java.io.*;
import java.net.InetAddress;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.Scanner;

public class ConnectionManager {

    private final int PORT;
    private BufferedReader in;
    private PrintWriter out;
    private Server server;
    private ArrayList<ClientSocket> clientSockets = new ArrayList(); // Saves the connection to peers

    public ConnectionManager(int PORT) {
        this.PORT = PORT;
        System.out.println("Server started at port " + PORT);
    }

    /*--------------------------------------------- General Management -----------------------------------------------*/
    public synchronized void dropEverything(){
        System.out.println("Dropping everything :(");
        server.stopRunning();

        for(ClientSocket cs : clientSockets){
            cs.stopRunning();
        }
    }

    /*--------------------------------------------------- Server -----------------------------------------------------*/
    public void startServing(){
        server = new Server(PORT, this);
        server.start();
    }

    public void stopServing(){
        server.stopRunning();
    }

    public synchronized void addAndStartClientSocket(ClientSocket clientSocket){
        clientSocket.start();
        clientSockets.add(clientSocket);
    }


    /*--------------------------------------------------- Client -----------------------------------------------------*/

    // Requests a connection for a client socket and saves it in a ClientSocket object in the ArrayList
    public synchronized boolean requestConnection(String address, String port) {
        int maxTries = 5;
        boolean connected = false;

        while (maxTries-- > 0) {
            ClientSocket client = new ClientSocket(address, port);

            if (client.connectToPeer()){
                client.start();
                clientSockets.add(client);
                return true;
            }
        }
        return false;
    }

    // Runs through the Arraylist to find the ClientSocket that takes care of the specific socket and sends message
    public boolean sendMessage(int port, String message) {
        for(ClientSocket client : clientSockets){
            if(client.getSocket().getPort() == port){
                client.sendMessage(message);
            }
        }
        return true;
    }

}
