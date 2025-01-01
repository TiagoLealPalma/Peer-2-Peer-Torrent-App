package src.Main.Connection;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;

public class Server extends Thread{

    private final ConnectionManager connectionManager;
    private final int port;
    private ServerSocket serverSocket;
    private volatile boolean running = true;

    public Server(int port, ConnectionManager connectionManager){
        this.port = port;
        this.connectionManager = connectionManager;
    }

    @Override
    public void run() {
        try {
            serverSocket = new ServerSocket(port);
            System.out.println("(" + connectionManager.getPORT() + ") Server listening on port " + port);
        } catch (IOException e) {System.out.println("(" + connectionManager.getPORT() + ") Error creating the server");}
        try {
            // Ears for new Connections
            while (running) {
                try {
                    Socket clientSocket = serverSocket.accept();
                    connectionManager.createConnection(clientSocket);

                } catch (IOException e) {
                    System.out.println("(" + connectionManager.getPORT() + ") Error accepting client connection");
                }
            }
        }finally {
            closeServer();
        }
    }


    public void stopRunning(){
        running = false;
        closeServer();
    }

    public void closeServer(){
        try{
            if (serverSocket != null && !serverSocket.isClosed()) {
                serverSocket.close();
            }
        } catch (IOException e) {
            System.out.println("Error closing server socket: " + e.getMessage());
        }
    }
}
