import java.io.*;
import java.net.InetAddress;
import java.net.Socket;
import java.net.UnknownHostException;

public class PeerSocket extends Thread{

    private BufferedReader in;
    private PrintWriter out;
    private Socket socket;
    private final String address;
    private final String port;
    private final String addressPort;
    private volatile boolean running = true;

    public PeerSocket(String address, String port) {
        this.address = address;
        this.port = port;
        this.addressPort = address+":"+port;
    }

    // To handle the sockets accepted by the server
    public PeerSocket(Socket socket) {
        this.socket = socket;
        this.address = socket.getInetAddress().toString();
        this.port = String.valueOf(socket.getPort());
        this.addressPort = address + ":" + port;
        setupStreams();
    }


    @Override
    public void run() {
        try {
            while (running) {
                try {
                    String received = in.readLine();

                    if (received == null) {
                        System.out.println(addressPort + "disconnected");
                        running = false;
                        break;
                    }

                    if (!received.equals("")) {
                        if (received.startsWith("GET")) {
                            handleGetRequest(received.substring(3));
                        } else if (received.startsWith("POST")) {
                            handlePostRequest(received.substring(4));
                        }
                    }

                } catch (IOException e) {
                    System.out.println("Error occurred while handling client: " + addressPort);
                    running = false;
                }
            }
        } finally { // Assures all resources used are cleaned before stepping out of the method
        closeConnection();
        }
    }



    public boolean connectToPeer() {
        InetAddress add;

        try {
            add = InetAddress.getByName("localhost");
            int portNumber = Integer.parseInt(port);
            socket = new Socket(add, portNumber);

            // Setup input/output streams

            return setupStreams();

        // Handles all possible exceptions
        } catch (UnknownHostException e) {
            System.out.println("Could not resolve the address: " + address);
        } catch (NumberFormatException e) {
            System.out.println("Port is not a valid number: " + port);
        } catch (IOException e) {
            System.out.println("Error occurred while connecting to the peer: " + addressPort);
        }
        return false;
    }

    private boolean setupStreams() {
        try {
            in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
            out = new PrintWriter(new BufferedWriter(new OutputStreamWriter(socket.getOutputStream())), true);
        } catch (IOException e) {
            System.out.println("Failed to connect or set up streams: " + e.getMessage());
            return false;
        }
        return true;
    }



    private void handleGetRequest(String request) {
        out.println("Handling client GET request");
    }

    private void handlePostRequest(String request) {
        out.println("Handling client POST request");
    }

    public boolean sendMessage(String message) {
        out.println(message);
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

    public Socket getSocket() {
        return socket;
    }
}
