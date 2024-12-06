package V2.Main.Connection;

import V2.Auxiliary.DownloadRelated.FileBlockRequest;
import V2.Auxiliary.DownloadRelated.FileBlockResult;
import V2.Auxiliary.SearchRelated.FileSearchResult;
import V2.Auxiliary.ConnectionRelated.NewConnectionRequest;
import V2.Auxiliary.SearchRelated.WordSearchRequest;
import V2.Auxiliary.Structs.FileMetadata;
import V2.Main.Coordinator;
import V2.Main.FileSharing.DownloadWorker;
import V2.Main.FileSharing.UploadProcess;
import V2.Main.Interface.UserInterface;
import V2.Main.Repository.Repo;

import java.io.*;
import java.net.*;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class OpenConnection extends Thread{

    private ObjectInputStream in;
    private ObjectOutputStream out;
    private Socket socket;
    private final int homePort;
    private final int correspondentPort; // Information related to the client
    private final String address;
    private final String addressPort;
    private final ConnectionManager connectionManager;

    // Maps to provide direct communication with download/upload processes
    private Map<String, UploadProcess> uploadProcesses = new HashMap();
    private Map<String, DownloadWorker> downloadWorkers = new HashMap();
    private volatile boolean running = true;

    // To open new connections
    public OpenConnection(ConnectionManager connectionManager, int correspondentPort) {
        this.address = "127.0.0.1";
        this.correspondentPort = correspondentPort;
        this.homePort = connectionManager.getPORT();
        this.addressPort = address+":"+ this.correspondentPort;
        this.connectionManager = connectionManager;
    }

    // To handle the sockets accepted by the server
    public OpenConnection(ConnectionManager connectionManager, Socket socket) {
        this.address = "127.0.0.1";
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
            sendWordSearchRequest(new WordSearchRequest(UserInterface.getInstance().getKeyword()));
            while (running) {
                try {

                    Object message = in.readObject();

                    if (message instanceof NewConnectionRequest){
                        NewConnectionRequest newConnectionRequest = (NewConnectionRequest) message;
                        handleNewConnectionRequest(newConnectionRequest);
                    } else if (message instanceof WordSearchRequest) {
                        WordSearchRequest wordSearchRequest = (WordSearchRequest) message;
                        handleWordSearchRequest(wordSearchRequest);
                    } else if (message instanceof FileSearchResult) {
                        FileSearchResult fileSearchResult = (FileSearchResult) message;
                        handleFileSearchResult(fileSearchResult);
                    }else if (message instanceof FileBlockRequest){
                        FileBlockRequest fileBlockRequest = (FileBlockRequest) message;
                        handleFileBlockRequest(fileBlockRequest);
                    } else if (message instanceof FileBlockResult){
                        FileBlockResult fileBlockResult = (FileBlockResult) message;
                        handleFileBlockResult(fileBlockResult);
                    }


                } catch (ClassNotFoundException e) {
                    System.out.println("(" + homePort + ") Message type not recognized: " + correspondentPort);
                } catch (IOException e) {
                    e.printStackTrace();
                    System.out.println("(" + homePort + ") Error in reading message: " + correspondentPort);
                }
            }
        } finally { // Assures all resources used are cleaned before stepping out of the method
            closeConnection();
        }
    }

    /* ------------------------------------------- Manage Connection --------------------------------------------------- */

    public boolean connectToPeer() {
        try {
            socket = new Socket(address, correspondentPort);
            return setupStreams();

            // Handles all possible exceptions
        } catch (IOException e) {
            System.out.println("(" + homePort + ") Error occurred while connecting to the peer: " + correspondentPort);
            return false;
        }
    }

    private boolean setupStreams() {
        try {
            out = new ObjectOutputStream(socket.getOutputStream());
            out.flush();
            in = new ObjectInputStream(socket.getInputStream());
        } catch (IOException e) {
            System.out.println("("+ homePort + ") Failed to connect or set up streams: " + e.getMessage());
            return false;
        }
        return true;
    }

    public void stopRunning(){
        System.out.println("("+ homePort + ") Stopping socket thread: " + addressPort);
        running = false;
        closeConnection();
        connectionManager.removeConnection(correspondentPort);
    }

    public void closeConnection() {
        try {
            if (socket != null && !socket.isClosed()) {
                socket.close();
                System.out.println("(" + homePort + ") Connection with " + addressPort + " closed");
            }
        } catch (IOException e) {
            System.out.println("(" + homePort + ") Failed to close socket: " + e.getMessage());
        }
    }


    /* --------------------------------------------- Handle Messages ------------------------------------------------ */

    private void handleNewConnectionRequest(NewConnectionRequest newConnectionRequest) {
        connectionManager.addNewConceptualConnection(this);
    }


    private void handleWordSearchRequest(WordSearchRequest wordSearchRequest) {
        FileSearchResult result = new FileSearchResult(
                Repo.getInstance().wordSearchResponse(wordSearchRequest.getKeyWord()), wordSearchRequest);
        sendMessage(result);
    }


    private void handleFileSearchResult(FileSearchResult fileSearchResult) {
        List<FileMetadata> result = fileSearchResult.getList();
        if(result.isEmpty()) return;

        connectionManager.receiveFileSearch(fileSearchResult.getList(), this);
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

    public void sendNewConnectionRequest(){
        try {
            out.writeObject(new NewConnectionRequest(correspondentPort));
            out.flush();
        } catch (IOException e) {
            System.out.println("(" + homePort + ") Failed to send new connection request to correspondentPort: " + correspondentPort);
        }
    }

    // Sends Word Search Request based on the keyWord value on the connection manager
    public void sendWordSearchRequest(WordSearchRequest wordSearchRequest) {
        try {
            System.out.println("(" + homePort + ") A enviar pedido de pesquisa por '" + connectionManager.getKeyWord() + "'");
            out.writeObject(wordSearchRequest);
            out.flush();

        } catch (IOException e) {System.out.println("(" + homePort + ") Error occurred while sending word search request to correspondentPort: " + correspondentPort);}
    }

    // Sends the file result to whom ever asked
    public void sendFileSearchResult(FileSearchResult result) {
        try{
            out.writeObject(result);
            out.flush();
        }catch (IOException e) {
            System.out.println("(" + homePort + ") Error occurred while sending word search result to correspondentPort: " + correspondentPort);
        }
    }

    public void sendFileBlockResult(FileBlockResult fileBlockResult) {
        try{

            out.writeObject(fileBlockResult);
            out.flush();
        } catch (IOException e) {
            System.out.println("(" + homePort + ") Error occurred while sending FileBlockResult to correspondentPort: " + correspondentPort);
        }
    }

    public void sendMessage(Serializable message){
        try{
            out.writeObject(message);
            out.flush();
        } catch (IOException e) {
            System.out.println("(" + homePort + ") Error occurred while sending message to correspondentPort: " + correspondentPort);
        }
    }

    public int getCorrespondentPort(){ return correspondentPort; }

    public void connectDownloadWorker(String processID, DownloadWorker downloadWorker) {
        if(!downloadWorkers.containsKey(processID) )
            downloadWorkers.put(processID, downloadWorker);
    }
}

