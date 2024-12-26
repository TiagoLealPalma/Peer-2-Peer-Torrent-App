package V2.Main.Connection;

import V2.Auxiliary.SearchRelated.FileSearchResult;
import V2.Auxiliary.SearchRelated.WordSearchRequest;
import V2.Auxiliary.Structs.FileMetadata;
import V2.Main.FileSharing.FileTransferManager;
import V2.Main.Interface.UserInterface;
import V2.Main.Repository.Repo;

import java.io.Serializable;
import java.net.Socket;
import java.util.*;

public class ConnectionManager {

    private final int PORT;
    private Server server;
    private String keyWord = "";
    private final Map<Integer, OpenConnection> openConnections = new HashMap<>(); // Direct Connections
    private final HashMap<FileMetadata, ArrayList<Integer>> filesAvailable = new HashMap<>(); // Saves files and the peers even if no direct link is established
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
        notifyAll();
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
    public void initiateWordSearchMessage(){
        String keyword = UserInterface.getInstance().getKeyword();
        List<Integer> connectionsAsked = new ArrayList<>(openConnections.keySet().stream().toList());
        connectionsAsked.add(PORT);
        Stack<Integer> path = new Stack<>();
        path.add(PORT);

        openConnections.values().forEach((c)->{
            c.sendMessage(new WordSearchRequest(keyword, connectionsAsked, path));
        });
    }

    public void propagateWordSearchRequest(WordSearchRequest wsr){
        if(wsr.getPath() == null || wsr.getConnectionsAsked() == null || wsr.getKeyWord() == null){ // Error occurred
            System.err.println("ERROR: Word search request could not be determined.");
            return;
        }
        if(wsr.getPath().contains(PORT) || wsr.getConnectionsAsked().contains(PORT)) return; // Cuts loops (Similar to BGP)

        // Check which connections have already been asked
        List<Integer> connectionsToSend = openConnections.keySet().stream()
                .filter(c -> !wsr.getConnectionsAsked().contains(c))
                .toList();

        if(wsr.getPath().isEmpty()){
            System.err.println("Path is empty");
            return;
        }

        // Backwards Response
        Stack<Integer> backwardsPath = (Stack<Integer>) wsr.getPath().clone();
        openConnections.get(backwardsPath.pop()).sendMessage(
                                new FileSearchResult(Repo.getInstance().wordSearchResponse(wsr.getKeyWord()), backwardsPath, PORT));


        // Check if list of connections greater than 0, if so propagate further
        if(!connectionsToSend.isEmpty()){
            // Add all the ports this is being propagated to, to the list of askedPorts
            List<Integer> connectionsAsked = new ArrayList<>(wsr.getConnectionsAsked());
            connectionsAsked.addAll(connectionsToSend);
            // Add this node to the path
            Stack<Integer> path = (Stack<Integer>) wsr.getPath().clone();
            path.add(PORT);
            // Send Message
            connectionsToSend.forEach(c -> openConnections.get(c).sendMessage(new WordSearchRequest(wsr.getKeyWord(), connectionsAsked, path)));
        }
    }

    public void deliverSearchToRootNode(FileSearchResult fileSearchResult) {
        OpenConnection connectionToSend = openConnections.get(fileSearchResult.pathPop());
        connectionToSend.sendMessage(fileSearchResult);
    }


    public synchronized void retrieveSearches(FileSearchResult fileSearchResult){
        for(FileMetadata file : fileSearchResult.getList()) {
            // If file is new to the list, add new entry
            if(!filesAvailable.containsKey(file)) filesAvailable.put(file, new ArrayList<>());

            // Add peer to list of peers for the given file
            for(Map.Entry<FileMetadata, ArrayList<Integer>> entry : filesAvailable.entrySet()) {
                if(entry.getKey().equals(file)) {
                    entry.getValue().add(fileSearchResult.getPort());
                    break;
                }
            }
        }
        // Atualizar UI
        UserInterface.getInstance().addContentToSearchList();
    }

    // Establishes connection with peers who are providing the file, and starts the upload process with those which
    // was able to connect
    public synchronized void prepareSeeders(String processId, FileMetadata fileToDownload) {
        ArrayList<Integer> seeders = filesAvailable.get(fileToDownload);

        // Go through the peers
        for (Integer peerId : seeders) {
            // Setup connections with those whom are not connected
            if(!openConnections.containsKey(peerId)){
                if(requestConnection("localhost", peerId) != 20) // If cant connect
                    break; // Skips this peer
            }
            // Start listening for given seeder
            try{wait(1000);} catch (InterruptedException e) {
                throw new RuntimeException(e);
            }
            FileTransferManager.getInstance().addNewSeederToDownloadProcess(processId, openConnections.get(peerId));
        }
    }

    public HashMap<FileMetadata, ArrayList<Integer>> getFilesAvailable() {
        return filesAvailable;
    }

    public int getPORT(){
        return PORT;
    }

    public void clearFilesAvailable(){
        filesAvailable.clear();
    }
}
