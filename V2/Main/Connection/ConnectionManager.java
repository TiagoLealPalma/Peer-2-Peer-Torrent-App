package V2.Main.Connection;

import V2.Auxiliary.SearchRelated.FileSearchResult;
import V2.Auxiliary.SearchRelated.WordSearchRequest;
import V2.Auxiliary.Structs.FileMetadata;
import V2.Main.FileSharing.FileTransferManager;
import V2.Main.Interface.UserInterface;
import V2.Main.Repository.Repo;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.Serializable;
import java.net.*;
import java.util.*;
import java.io.OutputStream;


import org.fourthline.cling.UpnpService;
import org.fourthline.cling.UpnpServiceImpl;
import org.fourthline.cling.model.meta.RemoteDevice;
import org.fourthline.cling.model.meta.Service;
import org.fourthline.cling.model.types.UDAServiceType;
import org.fourthline.cling.support.igd.PortMappingListener;
import org.fourthline.cling.support.model.PortMapping;

public class ConnectionManager {

    private static final String WebServerAddress = "https://sunny-lean-flyingfish.glitch.me";
    private Server server;
    private final Map<Integer, OpenConnection> openConnections = new HashMap<>(); // Direct Connections
    private final HashMap<FileMetadata, ArrayList<Integer>> filesAvailable = new HashMap<>(); // Saves files and the peers even if no direct link is established
    private static ConnectionManager instance;

    // Universal Plug n Play
    private String LOCAL_ADDRESS = "";
    private final int PORT; // External port (mapped through NAT)
    private static final String DESCRIPTION = "My Torrent Application";


    private ConnectionManager(int port) {
        // Get public ip need to get the public IP (STUN or Relay Server)
        this.PORT = port;
        InetAddress localHost = null;
        try {
            localHost = InetAddress.getLocalHost();
            LOCAL_ADDRESS = localHost.getHostAddress();
        } catch (UnknownHostException e) {
            System.err.println("Could not get local host address");
        }

        // Send register to Web Server
        if(Objects.equals(LOCAL_ADDRESS, "")){
            System.err.println("Could not get local host address, try restarting the client or checking internet connection");
            return;
        }

        getPnPMapping();
        registerOnWebServer();
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

    private void getPnPMapping(){

        // Create a mapping of the chosen PORT for NAT traversal
        PortMapping portMapping = new PortMapping(
                PORT, LOCAL_ADDRESS, PortMapping.Protocol.TCP, "Test UPnP Mapping"
        );
        PortMappingListener portMappingListener = new PortMappingListener(portMapping);

        // Start the UPnP service with the PortMappingListener
        UpnpService upnpService = new UpnpServiceImpl(portMappingListener);

        // Start searching for UPnP-enabled devices
        upnpService.getControlPoint().search();

        // Hook to stop the UPnP service and clean up on application exit
        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            System.out.println("Shutting down UPnP service...");
            upnpService.shutdown();
        }));

        System.out.println("UPnP Port Mapping started.");
        System.out.println("External Port " + PORT + " is mapped to " + LOCAL_ADDRESS + ":" + PORT);
    }

    private void registerOnWebServer() {
        try {
            // Create the URL object
            URL url = new URL(WebServerAddress+"/register");

            // Open the connection
            HttpURLConnection connection = (HttpURLConnection) url.openConnection();
            connection.setRequestMethod("POST");
            connection.setRequestProperty("Content-Type", "application/x-www-form-urlencoded");
            connection.setDoOutput(true);

            // Create the Url-form-encoded payload
            String payload = String.format("ip=%s&port=%d", LOCAL_ADDRESS, PORT);

            // Send the payload
            try (OutputStream os = connection.getOutputStream()) {
                os.write(payload.getBytes());
                os.flush();
            }

            // Get the response code
            int responseCode = connection.getResponseCode();
            if (responseCode == HttpURLConnection.HTTP_OK) {
                System.out.println("Peer registered successfully!");
            } else {
                System.out.println("Failed to register peer. Response code: " + responseCode);
            }

        } catch (Exception e) {
            System.err.println("Failed to register peer.");
        }
    }

    public List<String> getRegistedPeers(){
        try {
            // Create URL object
            URL url = new URL(WebServerAddress+"/peers");

            // Open connection
            HttpURLConnection connection = (HttpURLConnection) url.openConnection();
            connection.setRequestMethod("GET");

            // Get response code
            int responseCode = connection.getResponseCode();
            if (responseCode == HttpURLConnection.HTTP_OK) { // Success
                // Read the response
                BufferedReader in = new BufferedReader(new InputStreamReader(connection.getInputStream()));
                String inputLine;
                StringBuilder response = new StringBuilder();

                while ((inputLine = in.readLine()) != null) {
                    response.append(inputLine).append("\n");
                }
                in.close();

                // Format the data into a list
                List<String> peers = new ArrayList<>(List.of(response.toString().split("\n")));
                return peers;

            } else {
                System.out.println("Failed to retrieve peers. Response code: " + responseCode);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        return Collections.emptyList();
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
            OpenConnection connection = new OpenConnection(this, port, address);

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
       // if(wsr.getPath().contains(PORT) || wsr.getConnectionsAsked().contains(PORT)) return; // Cuts loops (Similar to BGP)

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
