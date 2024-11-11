package V2;

import V2.Structs.FileMetadata;

import java.net.Socket;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ConnectionManager {

    private final int PORT;
    private V2.Controller controller;
    private V2.Server server;
    private String keyWord = "";
    private Map<Integer, OpenConnection> openConnections = new HashMap();

    public ConnectionManager(Controller controller, int PORT) {
        this.PORT = PORT;
        System.out.println("Server started at port " + PORT);
        this.controller = controller;

        startServing(); // Starts to listen to incoming requests
    }

    /*--------------------------------------------- General Management -----------------------------------------------*/


    /*--------------------------------------------------- Server -----------------------------------------------------*/
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
        if(!openConnections.values().contains(connection.getCorrespondentPort())) {
            System.out.println("Recebido new connection request from port: " + connection.getCorrespondentPort());
            openConnections.put(connection.getCorrespondentPort(), connection);
        }

    }


    /*--------------------------------------------------- Client -----------------------------------------------------*/

    // Requests a connection for a client socket and saves it in the openConnections HashMap
    public synchronized OpenConnection requestConnection(String address, int port) {
        int maxTries = 5;
        boolean connected = false;

        while (maxTries-- > 0) {
            OpenConnection connection = new OpenConnection(this, port);

            if (connection.connectToPeer()){
                connection.start();
                return connection;
            }
        }
        return null;
    }

    public void addConnection(OpenConnection connection) {
        openConnections.put(connection.getCorrespondentPort(), connection);
    }

    public synchronized void removeConnection(int port) {
        if(openConnections.containsKey(port)) {
            openConnections.remove(port);
        }
    }

    /*-------------------------------------------------- Requests ----------------------------------------------------*/

    public synchronized void floodWordSearchRequest(String keyWord) {
        // Inundar todas as ligações abertas com Word Requests
        for (OpenConnection connection : openConnections.values()) {
            connection.sendWordSearchRequest( true);
        }

    }

    /*--------------------------------------------------- Tunneling -----------------------------------------------------*/

    public List<FileMetadata> wordSearchResponse(String keyWord) {
        return controller.wordSearchResponse(keyWord);
    }

    public void updateUiList(List<FileMetadata> list) {
        controller.updateUiList(list);
    }

    public synchronized void setKeyWord(String keyWord) {
        this.keyWord = keyWord;
    }

    public synchronized String getKeyWord() {
        return keyWord;
    }
    public int getPORT(){
        return PORT;
    }
}
