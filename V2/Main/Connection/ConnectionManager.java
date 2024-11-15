package V2.Main.Connection;

import V2.Auxiliary.MessageTypes.DownloadRelated.FileDownloadRequest;
import V2.Auxiliary.MessageTypes.DownloadRelated.FileDownloadResponse;
import V2.Auxiliary.MessageTypes.SearchRelated.WordSearchRequest;
import V2.Auxiliary.Structs.FileMetadata;
import V2.Main.Controller;
import V2.Main.FileSharing.UploadProcess;

import java.io.Serializable;
import java.net.Socket;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ConnectionManager {

    private final int PORT;
    private Controller controller;
    private Server server;
    private String keyWord = "";
    private Map<Integer, OpenConnection> openConnections = new HashMap();


    public ConnectionManager(Controller controller, int PORT) {
        this.PORT = PORT;
        this.controller = controller;

        startServing(); // Starts to listen to incoming requests
    }

    /*--------------------------------------------- General Management -----------------------------------------------*/


    /*--------------------------------------------------- Server -----------------------------------------------------*/
    public void startServing(){
        server = new Server(PORT, this);
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
    // Outcome Handling: 10 (Non-Valid Connection); 20 (Success); 30 (Connection failed);
    public synchronized int requestConnection(String address, int port) {
        int maxTries = 5;
        boolean connected = false;

        // Corner Cases
        if(openConnections.containsKey(port)) return 11;  // Connection already established
        if(port == controller.PORT)           return 12;  // Trying to connect with itself
        if(port < 8080 || port > 65535)       return 13;  // Trying to connect with non-permitted ports

        // Try to open a connection for a maximum of 4 retries
        while (maxTries-- > 0) {
            OpenConnection connection = new OpenConnection(this, port);

            if (connection.connectToPeer()){
                connection.start();
                return 20;
            }
        }
        return 30;
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

    public synchronized void floodMessage(Serializable message) {
        // Inundar todas as ligações abertas com Word Requests
        for (OpenConnection connection : openConnections.values()) {
            if(message instanceof WordSearchRequest){
                WordSearchRequest request = (WordSearchRequest) message;
                connection.sendWordSearchRequest(request);
            }
            else if(message instanceof FileDownloadRequest){
                FileDownloadRequest request = (FileDownloadRequest) message;
                connection.sendFileDownloadRequest(request);
            }
            else{
                System.out.println("(" + getPORT() + ") Flooding does not support the following message type: \n" + message.toString());
                return;
            }
        }
    }

    /*--------------------------------------------------- Tunneling -----------------------------------------------------*/

    public List<FileMetadata> wordSearchResponse(String keyWord) {
        return controller.wordSearchResponse(keyWord);
    }

    public void updateUiList(List<FileMetadata> list) {
        controller.updateUiList(list);
    }

    public void attemptToStartUploadProcess(OpenConnection connectionWithDownloadingPeer,
                                                                        FileDownloadRequest request){
        controller.attemptToStartUploadProcess(connectionWithDownloadingPeer, request);
    }

    public void addNewSeederToDownloadProcess(FileDownloadResponse fileDownloadResponse, OpenConnection connection) {
        controller.addNewSeederToDownloadProcess(fileDownloadResponse, connection);
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

    public void sendDownloadResponse(OpenConnection connection, String id, UploadProcess process) {
        connection.sendFileDownloadResponse(id, process);
    }


}
