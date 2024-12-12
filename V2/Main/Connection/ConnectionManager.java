package V2.Main.Connection;

import V2.Auxiliary.Structs.FileMetadata;
import V2.Main.FileSharing.FileTransferManager;
import V2.Main.Interface.UserInterface;
import java.io.Serializable;
import java.net.Socket;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ConnectionManager {

    private final int PORT;
    private Server server;
    private String keyWord = "";
    private final Map<Integer, OpenConnection> openConnections = new HashMap<>();
    private final HashMap<FileMetadata, ArrayList<OpenConnection>> filesAvailable = new HashMap<>();
    private static ConnectionManager instance;


    private ConnectionManager(int port) {
        this.PORT = port;
        startServing();
    }

    public static synchronized ConnectionManager getInstance(int port) {
        if (instance == null) {
            instance = new ConnectionManager(port);
        }
        return instance;
    }

    public static synchronized ConnectionManager getInstance() {
        if (instance == null) {
            throw new IllegalStateException("ConnectionManager has not been initialized yet.");
        }
        return instance;
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
        openConnections.put(connection.getCorrespondentPort(), connection);
    }


    /*--------------------------------------------------- Client -----------------------------------------------------*/

    // Requests a connection for a client socket and saves it in the openConnections HashMap
    // Outcome Handling: 10 (Non-Valid Connection); 20 (Success); 30 (Connection failed);
    public synchronized int requestConnection(String address, int port) {
        int maxTries = 5;

        // Corner Cases
        if(openConnections.containsKey(port)) return 11;  // Connection already established
        if(port == PORT)                      return 12;  // Trying to connect with itself
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


    public synchronized void removeConnection(int port) {
        if(openConnections.containsKey(port)) {
            openConnections.remove(port);
        }
    }

    /*-------------------------------------------------- Requests ----------------------------------------------------*/

    public synchronized void floodMessage(Serializable message) {
        for (OpenConnection connection : openConnections.values())
                connection.sendMessage(message);
    }

    /*--------------------------------------------------- Tunneling -----------------------------------------------------*/

    public synchronized void receiveFileSearch(List<FileMetadata> list, OpenConnection connection) {
        // Guardar os ficheiros e os peer que os disponibilizam em memoria
        for(FileMetadata file : list) {
            if(!filesAvailable.containsKey(file)) {filesAvailable.put(file, new ArrayList<>());}
            for(Map.Entry<FileMetadata, ArrayList<OpenConnection>> entry : filesAvailable.entrySet()) {
                if(entry.getKey().equals(file)) {
                    entry.getValue().add(connection);
                    break;
                }
            }
        }

        // Atualizar UI
        UserInterface.getInstance().addContentToSearchList(list);
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

    public void prepareSeeders(String processId, FileMetadata fileToDownload) {
        ArrayList<OpenConnection> seeders = filesAvailable.get(fileToDownload);

        for (OpenConnection connection : seeders) {
            FileTransferManager.getInstance().addNewSeederToDownloadProcess(processId, connection);
        }
    }
}
