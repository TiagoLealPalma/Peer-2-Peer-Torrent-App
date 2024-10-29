package V1;

import java.io.*;
import java.util.ArrayList;

public class ConnectionManager {

    private final int PORT;
    private Controller controller;
    private Server server;
    private ArrayList<PeerSocket> peerSockets = new ArrayList(); // Saves the connection to peers

    public ConnectionManager(Controller controller, int PORT) {
        this.PORT = PORT;
        System.out.println("V1.Server started at port " + PORT);
        this.controller = controller;

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

    /*--------------------------------------------------- V1.Server -----------------------------------------------------*/
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
        peerSocket.sendMessage("GETSEARCH"); // Pedir Atualização da search list

    }


    /*--------------------------------------------------- Client -----------------------------------------------------*/

    // Requests a connection for a client socket and saves it in a V1.PeerSocket object in the ArrayList
    // Also retrieves the titles for the files from the peers whom have been connected to
    public synchronized boolean requestConnection(String address, String port) {
        int maxTries = 5;
        boolean connected = false;

        while (maxTries-- > 0) {
            PeerSocket peer = new PeerSocket(this, address, port);

            if (peer.connectToPeer()){
                peer.start();
                peerSockets.add(peer);
                peer.sendMessage("GETSEARCH");
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

    /*--------------------------------------------------- Channel -----------------------------------------------------*/

    public void updateSearchList(String[] searchList){
        controller.updateSearchList(searchList);
    }


    public ArrayList<String> getSearchContent() {
        return controller.getSearchContent();
    }
}
