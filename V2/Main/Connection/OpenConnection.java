package V2.Main.Connection;

import V2.Auxiliary.MessageTypes.DownloadRelated.FileBlockRequest;
import V2.Auxiliary.MessageTypes.DownloadRelated.FileBlockResult;
import V2.Auxiliary.MessageTypes.DownloadRelated.FileDownloadRequest;
import V2.Auxiliary.MessageTypes.DownloadRelated.FileDownloadResponse;
import V2.Auxiliary.MessageTypes.SearchRelated.FileSearchResult;
import V2.Auxiliary.MessageTypes.ConnectionRelated.NewConnectionRequest;
import V2.Auxiliary.MessageTypes.SearchRelated.WordSearchRequest;
import V2.Auxiliary.Structs.FileMetadata;
import V2.Main.FileSharing.DownloadWorker;
import V2.Main.FileSharing.UploadProcess;

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
            sendNewConnectionRequest();
            sendWordSearchRequest(new WordSearchRequest(connectionManager.getKeyWord()));
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
                    } else if (message instanceof FileDownloadRequest){
                        FileDownloadRequest fileDownloadRequest = (FileDownloadRequest) message;
                        handleFileDownloadRequest(fileDownloadRequest);
                    } else if (message instanceof FileDownloadResponse){
                        FileDownloadResponse fileDownloadResponse = (FileDownloadResponse) message;
                        handleFileDownloadResponse(fileDownloadResponse);
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
                    System.out.println("(" + homePort + ") Error in reading message: " + correspondentPort);
                    e.printStackTrace();
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
                                            connectionManager.wordSearchResponse(wordSearchRequest.getKeyWord()));
        sendFileSearchResult(result);
    }


    private void handleFileSearchResult(FileSearchResult fileSearchResult) {
        List<FileMetadata> result = fileSearchResult.getList();
        if(result.isEmpty()) return;

        connectionManager.updateUiList(fileSearchResult.getList());
    }


    private void handleFileDownloadRequest(FileDownloadRequest fileDownloadRequest) {
        if(uploadProcesses.containsKey(fileDownloadRequest.getId())) return; // If this process has already been started
        connectionManager.attemptToStartUploadProcess(this, fileDownloadRequest);
    }


    private void handleFileDownloadResponse(FileDownloadResponse fileDownloadResponse) {
        connectionManager.addNewSeederToDownloadProcess(fileDownloadResponse, this);
    }


    private void handleFileBlockRequest(FileBlockRequest fileBlockRequest) {
        UploadProcess process = uploadProcesses.get(fileBlockRequest.getId());
        if(process == null) return;

        process.requestBlock(fileBlockRequest);
    }


    private void handleFileBlockResult(FileBlockResult fileBlockResult) {
        DownloadWorker worker = downloadWorkers.get(fileBlockResult.getId());
        worker.submitFileBlockResult(fileBlockResult);
    }

    /* ---------------------------------------------- Send Messages ------------------------------------------------- */

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

    // Send download request to peer
    public void sendFileDownloadRequest(FileDownloadRequest fileDownloadRequest) {
        try{
            out.writeObject(fileDownloadRequest);
            out.flush();
        } catch (IOException e) {
            System.out.println("(" + homePort + ") Error occurred while sending download request to correspondentPort: " + correspondentPort);
        }
    }

    // Create direct communication between this connection thread and upload thread, so
    // the main thread cant bottleneck the transfer
    public void sendFileDownloadResponse(String id, UploadProcess uploadProcess) {
        if(!uploadProcesses.containsKey(uploadProcess.getId()))
            uploadProcesses.put(id, uploadProcess);

        try {
            out.writeObject(new FileDownloadResponse(id));
            out.flush();
        }catch (IOException e) {
            System.out.println("(" + homePort + ") Error occurred while sending FileDownloadResponse to correspondentPort: " + correspondentPort);
        }
    }

    public void sendFileBlockRequest(FileBlockRequest fileBlockRequest, DownloadWorker downloadWorker) {
        if(!downloadWorkers.containsKey(downloadWorker.getId()))
            downloadWorkers.put(fileBlockRequest.getId(), downloadWorker);

        try{
            out.writeObject(fileBlockRequest);
            out.flush();
        }catch (IOException e){
            System.out.println("(" + homePort + ") Error occurred while sending FileBlockRequest to correspondentPort: " + correspondentPort);
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

    public Socket getSocket() { return socket; }

    public int getCorrespondentPort(){ return correspondentPort; }



}

