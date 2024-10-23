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
    private ArrayList<PeerSocket> peerSockets = new ArrayList(); // Saves the connection to peers

    public ConnectionManager(int PORT) {
        this.PORT = PORT;
        System.out.println("Server started at port " + PORT);

        startServing(); // Starts to listen to incoming requests
    }

    /*--------------------------------------------- General Management -----------------------------------------------*/
    public synchronized void dropEverything(){
        System.out.println("Dropping everything :(");
        server.stopRunning();

        for(PeerSocket cs : peerSockets){
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

    public synchronized void addAndStartClientSocket(PeerSocket peerSocket){
        peerSocket.start();
        peerSockets.add(peerSocket);
    }


    /*--------------------------------------------------- Client -----------------------------------------------------*/

    // Requests a connection for a client socket and saves it in a PeerSocket object in the ArrayList
    public synchronized boolean requestConnection(String address, String port) {
        int maxTries = 5;
        boolean connected = false;

        while (maxTries-- > 0) {
            PeerSocket peer = new PeerSocket(address, port);

            if (peer.connectToPeer()){
                peer.start();
                peerSockets.add(peer);
                return true;
            }
        }
        return false;
    }

    // Runs through the Arraylist to find the ClientSocket that takes care of the specific socket and sends message
    public boolean sendMessage(int port, String message) {
        for(PeerSocket client : peerSockets){
            if(client.getSocket().getPort() == port){
                client.sendMessage(message);
            }
        }
        return true;
    }

}
