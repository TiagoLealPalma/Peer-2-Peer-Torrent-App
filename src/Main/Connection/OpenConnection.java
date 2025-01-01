package src.Main.Connection;

import src.Auxiliary.DownloadRelated.FileBlockRequest;
import src.Auxiliary.DownloadRelated.FileBlockResult;
import src.Auxiliary.SearchRelated.FileSearchResult;
import src.Auxiliary.ConnectionRelated.NewConnectionRequest;
import src.Auxiliary.SearchRelated.WordSearchRequest;
import src.Main.Coordinator;
import src.Main.FileSharing.DownloadWorker;
import src.Main.FileSharing.UploadProcess;
import src.Main.Interface.UserInterface;

import java.io.*;
import java.net.*;
import java.util.HashMap;
import java.util.Map;

public class OpenConnection extends Thread{

    private ObjectInputStream in;
    private ObjectOutputStream out;
    private Socket socket;
    private final int homePort;
    private int correspondentPort; // Information related to the client
    private final String address;
    private final String addressPort;
    private final ConnectionManager connectionManager;

    // Maps to provide direct communication with download/upload processes
    private Map<String, UploadProcess> uploadProcesses = new HashMap();
    private Map<String, DownloadWorker> downloadWorkers = new HashMap();
    private volatile boolean running = true;

    // To open new connections
    public OpenConnection(ConnectionManager connectionManager, int correspondentPort, String destAddress) {
        this.address = destAddress;
        this.correspondentPort = correspondentPort;
        this.homePort = connectionManager.getPORT();
        this.addressPort = address+":"+ this.correspondentPort;
        this.connectionManager = connectionManager;
    }

    // To handle the sockets accepted by the server
    public OpenConnection(ConnectionManager connectionManager, Socket socket) {
        this.address = socket.getInetAddress().getHostAddress();
        this.socket = socket;
        this.correspondentPort = socket.getPort();
        this.homePort = connectionManager.getPORT();
        this.addressPort = address + ":" + correspondentPort;
        this.connectionManager = connectionManager;
        setupStreams();
    }


    // Main loop
    @Override
    public void run() {
        try {
            sendMessage(new NewConnectionRequest(connectionManager.getPORT()));
            //sendMessage(new WordSearchRequest(UserInterface.getInstance().getKeyword()));
            while (running) {
                try {

                    Object message = in.readObject();

                    if (message instanceof NewConnectionRequest newConnectionRequest){
                        handleNewConnectionRequest(newConnectionRequest);
                    } else if (message instanceof WordSearchRequest wordSearchRequest) {
                        handleWordSearchRequest(wordSearchRequest);
                    } else if (message instanceof FileSearchResult fileSearchResult) {
                        handleFileSearchResult(fileSearchResult);
                    }else if (message instanceof FileBlockRequest request){
                        handleFileBlockRequest(request);
                    } else if (message instanceof FileBlockResult fileBlockResult){
                        handleFileBlockResult(fileBlockResult);
                    }


                } catch (ClassNotFoundException e) {
                    consoleLog("Message type not recognized.");
                } catch (IOException e) {
                    consoleLog("Connection lost with peer.");
                    stopRunning();
                }
            }
        } finally { // Assures all resources used are cleaned before stepping out of the method
            closeConnection();
        }
    }

    /* ------------------------------------------- Manage Connection ------------------------------------------------ */

    public boolean connectToPeer() {
        try {
            socket = new Socket(address, correspondentPort);
            return setupStreams();

            // Handles all possible exceptions
        } catch (IOException e) {
            consoleLogError("Unable to connect to peer");
            return false;
        }
    }

    private boolean setupStreams() {
        try {
            out = new ObjectOutputStream(socket.getOutputStream());
            out.flush();
            in = new ObjectInputStream(socket.getInputStream());
        } catch (IOException e) {
            consoleLogError("Streams could not be opened.");
            return false;
        }
        return true;
    }

    public void stopRunning(){
        running = false;
        closeConnection();
        connectionManager.removeConnection(correspondentPort);
        UserInterface.getInstance().searchKeyword(); // Updates the client search list, so only the available files are shown
    }

    public void closeConnection() {
        try {
            if (socket != null && !socket.isClosed()) {
                socket.close();
                consoleLog(" Connection closed");
            }
        } catch (IOException e) {
            consoleLog(" Failed to close socket.");
        }
    }


    /* ------------------------------------------------ Handlers ---------------------------------------------------- */

    private void handleNewConnectionRequest(NewConnectionRequest newConnectionRequest) {
        correspondentPort = newConnectionRequest.getPort();
        connectionManager.addNewConceptualConnection(this);
    }


    private void handleWordSearchRequest(WordSearchRequest wordSearchRequest) {
        connectionManager.propagateWordSearchRequest(wordSearchRequest);
    }


    private void handleFileSearchResult(FileSearchResult fileSearchResult) {
        // If this is root node retrieve searches
        if (fileSearchResult.isRootNode())
            connectionManager.retrieveSearches(fileSearchResult);
        else // If not propagate backwards according to the path
            connectionManager.deliverSearchToRootNode(fileSearchResult);


    }

    private void handleFileBlockRequest(FileBlockRequest fileBlockRequest) {
        UploadProcess process = uploadProcesses.get(fileBlockRequest.getId());

        // Se o processo ainda não tiver iniciado
        if(process == null) {
            process = Coordinator.getInstance().StartUploadProcess(this, fileBlockRequest);
            if(process == null)  return;// sendMessage(); necessita tratar do erro caso alguem entretanto apague um ficheiro da diretoria
            uploadProcesses.put(fileBlockRequest.getId(), process);
        }
        process.requestBlock(fileBlockRequest);
    }

    private void handleFileBlockResult(FileBlockResult fileBlockResult) {
        DownloadWorker worker = downloadWorkers.get(fileBlockResult.getId());
        worker.submitFileBlockResult(fileBlockResult);
    }

    /* ---------------------------------------------- Send Message -------------------------------------------------- */

    public synchronized void sendMessage(Serializable message){
        try{
            out.writeObject(message);
            out.flush();
        } catch (IOException e) {
            consoleLog(" Error occurred while sending message.");
        }
    }

    public int getCorrespondentPort(){ return correspondentPort; }

    public void connectDownloadWorker(String processID, DownloadWorker downloadWorker) {
        if(!downloadWorkers.containsKey(processID) )
            downloadWorkers.put(processID, downloadWorker);
    }

    public void consoleLog(String s) {
        System.out.println(String.format("(%d ---> %d): %s", homePort, correspondentPort, s));
    }

    public void consoleLogError(String s) {
        System.out.println(String.format("(%d): %s", homePort, s));
    }
}

