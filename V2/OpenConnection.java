package V2;

import V1.ConnectionManager;
import V2.MessageTypes.EndComms;
import V2.MessageTypes.FileSearchResult;
import V2.MessageTypes.NewConnectionRequest;
import V2.MessageTypes.WordSearchRequest;

import java.io.*;
import java.net.InetAddress;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.List;

public class OpenConnection extends Thread{

    private ObjectInputStream in;
    private ObjectOutputStream out;
    private Socket socket;
    private final int port; // Information related to the client
    private final String address;
    private final String addressPort;
    private final V2.ConnectionManager connectionManager;
    private boolean clearToEndComms = false;
    private volatile boolean running = true;

    // To open new connections
    public OpenConnection(V2.ConnectionManager connectionManager, int port) {
        this.address = "127.0.0.1";
        this.port = port;
        this.addressPort = address+":"+port;
        this.connectionManager = connectionManager;
    }

    // To handle the sockets accepted by the server
    public OpenConnection(V2.ConnectionManager connectionManager, Socket socket) {
        this.address = "127.0.0.1";
        this.socket = socket;
        this.port = socket.getPort();
        this.addressPort = address + ":" + port;
        this.connectionManager = connectionManager;
        setupStreams();
    }


    @Override
    public void run() {
        try {
            sendNewConnectionRequest();
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
                    } else if (message instanceof EndComms) {
                        clearToEndComms = true;
                    }

                    if(clearToEndComms) stopRunning();


                } catch (ClassNotFoundException e) {
                    System.out.println("Message type not recognized: " + addressPort);
                } catch (IOException e) {
                    System.out.println("Error in reading message: " + addressPort);
                }
            }
        } finally { // Assures all resources used are cleaned before stepping out of the method
            closeConnection();
        }
    }






    /* ------------------------------------------- Manage Connection --------------------------------------------------- */

    public boolean connectToPeer() {
        try {
            socket = new Socket(address, port);
            return setupStreams();

            // Handles all possible exceptions
        } catch (IOException e) {
            System.out.println("Error occurred while connecting to the peer: " + addressPort);
            return false;
        }
    }

    private boolean setupStreams() {
        try {
            out = new ObjectOutputStream(socket.getOutputStream());
            out.flush();
            in = new ObjectInputStream(socket.getInputStream());
        } catch (IOException e) {
            System.out.println("Failed to connect or set up streams: " + e.getMessage());
            return false;
        }
        return true;
    }

    public void stopRunning(){
        System.out.println("Stopping socket thread: Port " +addressPort);
        running = false;
        interrupt();
        closeConnection();
    }

    public void closeConnection() {
        try {
            if (socket != null && !socket.isClosed()) {
                socket.close();
                System.out.println("Connection with" + addressPort + "closed");
            }
        } catch (IOException e) {
            System.out.println("Failed to close socket: " + e.getMessage());
        }
    }


    /* --------------------------------------------- Handle Messages ------------------------------------------------ */

    private void handleNewConnectionRequest(NewConnectionRequest newConnectionRequest) {
        System.out.println("Recebido new connection request from port: " + port);
        connectionManager.addNewConceptualConnection(this);
    }

    private void handleWordSearchRequest(WordSearchRequest wordSearchRequest) {
        String keyWord = wordSearchRequest.getKeyWord();
        boolean uiUpdate = wordSearchRequest.isUIUpdate();
        FileSearchResult result = new FileSearchResult(connectionManager.wordSearchResponse(keyWord), uiUpdate);
        sendFileSearchResult(result);
    }

    private void handleFileSearchResult(FileSearchResult fileSearchResult) {
        List<String> result = fileSearchResult.getTitles();
        if(result.isEmpty()) return;
        if(fileSearchResult.isUIUpdate()) {
            connectionManager.updateUiList(fileSearchResult.getTitles());
            sendClearToEndComms();
        }
    }


    /* ---------------------------------------------- Send Messages ------------------------------------------------- */

    public void sendNewConnectionRequest(){
        try {
            out.writeObject(new NewConnectionRequest(port));
            out.flush();
        } catch (IOException e) {
            System.out.println("Failed to send new connection request to port: " + port);
        }
    }

    public void sendWordSearchRequest(String keyWord, boolean uiUpdate) {
        try {
            out.writeObject(new WordSearchRequest(keyWord, uiUpdate));
            out.flush();
        } catch (IOException e) {System.out.println("Error occurred while sending word search request to port: " + port);}
    }

    public void sendFileSearchResult(FileSearchResult result) {
        try{
            out.writeObject(result);
            out.flush();
        }catch (IOException e) {
            System.out.println("Error occurred while sending word search result to port: " + port);
        }
    }

    public void sendClearToEndComms(){
        try {
            out.writeObject(new EndComms());
        out.flush();
        } catch (IOException e) {
            System.out.println("Error occurred while sending end comms: " + port);
        }
    }







    public Socket getSocket() {
        return socket;
    }

    public int getPort(){
        return port;
    }


}

