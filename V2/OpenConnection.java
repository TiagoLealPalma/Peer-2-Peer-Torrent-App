package V2;

import V2.MessageTypes.FileSearchResult;
import V2.MessageTypes.NewConnectionRequest;
import V2.MessageTypes.WordSearchRequest;
import V2.Structs.FileMetadata;

import java.io.*;
import java.net.*;
import java.util.List;

public class OpenConnection extends Thread{

    private ObjectInputStream in;
    private ObjectOutputStream out;
    private Socket socket;
    private final int homePort;
    private final int correspondentPort; // Information related to the client
    private final String address;
    private final String addressPort;
    private final V2.ConnectionManager connectionManager;
    private volatile boolean running = true;

    // To open new connections
    public OpenConnection(V2.ConnectionManager connectionManager, int correspondentPort) {
        this.address = "127.0.0.1";
        this.correspondentPort = correspondentPort;
        this.homePort = connectionManager.getPORT();
        this.addressPort = address+":"+ this.correspondentPort;
        this.connectionManager = connectionManager;
    }

    // To handle the sockets accepted by the server
    public OpenConnection(V2.ConnectionManager connectionManager, Socket socket) {
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
            sendWordSearchRequest(true);
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
                                            connectionManager.wordSearchResponse(wordSearchRequest.getKeyWord())
                                                                            , wordSearchRequest.isUIUpdate());
        sendFileSearchResult(result);
    }

    private void handleFileSearchResult(FileSearchResult fileSearchResult) {
        List<FileMetadata> result = fileSearchResult.getList();

        if(result.isEmpty()) return;
        if(fileSearchResult.isUIUpdate()) {
            connectionManager.updateUiList(fileSearchResult.getList());
        }
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
    public void sendWordSearchRequest(boolean uiUpdate) {
        try {
            System.out.println("(" + homePort + ") A enviar pedido de pesquisa por '" + connectionManager.getKeyWord() + "'");
            out.writeObject(new WordSearchRequest(connectionManager.getKeyWord(), uiUpdate));
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

    public Socket getSocket() { return socket; }

    public int getCorrespondentPort(){ return correspondentPort; }
}

