package V2;

import V1.PeerSocket;
import V1.Server;

import java.io.*;
import java.net.Socket;
import java.util.ArrayList;
import java.util.List;

public class ConnectionManager {

    private final int PORT;
    private V2.Controller controller;
    private V2.Server server;
    private ArrayList<Integer> conceptualConnections = new ArrayList(); // Saves the connection to peers

    public ConnectionManager(Controller controller, int PORT) {
        this.PORT = PORT;
        System.out.println("Server started at port " + PORT);
        this.controller = controller;

        startServing(); // Starts to listen to incoming requests
    }

    /*--------------------------------------------- General Management -----------------------------------------------*/


    /*--------------------------------------------------- V1.Server -----------------------------------------------------*/
    public void startServing(){
        server = new V2.Server(PORT, this);
        server.start();
    }

    public void stopServing(){
        server.stopRunning();
    }

    public void createConnection(Socket clientSocket){
        OpenConnection connection = new OpenConnection(this, clientSocket);
        connection.start(); // Open communication channel
    }

    public synchronized void addNewConceptualConnection(OpenConnection connection){
        if(!conceptualConnections.contains(connection.getPort())) {
            conceptualConnections.add(connection.getPort());
            connection.sendWordSearchRequest("", true); // Pedir Atualização da search list
        }

    }


    /*--------------------------------------------------- Client -----------------------------------------------------*/

    // Requests a connection for a client socket and saves it in a V1.PeerSocket object in the ArrayList
    // Also retrieves the titles for the files from the peers whom have been connected to
    public synchronized boolean requestConnection(String address, String port) {
        int maxTries = 5;
        boolean connected = false;

        while (maxTries-- > 0) {
            OpenConnection connection = new OpenConnection(this, Integer.parseInt(port));

            if (connection.connectToPeer()){
                connection.start();
                return true;
            }
        }
        return false;
    }

    // Runs through the Arraylist to find the ClientSocket that takes care of the specific socket and sends message
    public boolean sendMessage(int port, String message) {
        for(Integer clientPorts : conceptualConnections){
        }
        return true;
    }

    /*--------------------------------------------------- Channel -----------------------------------------------------*/

    public List<String> wordSearchResponse(String keyWord) {
        return controller.wordSearchResponse(keyWord);
    }

    public void updateUiList(List<String> titles) {
        controller.updateUiList(titles);
    }
}
