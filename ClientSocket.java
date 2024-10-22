import java.io.*;
import java.net.InetAddress;
import java.net.Socket;
import java.net.UnknownHostException;

public class ClientSocket {

    private BufferedReader in;
    private PrintWriter out;
    private Socket socket;

    public boolean connectToPeer(String address, String port) {
        InetAddress add;
        try {
            // Resolves the address
            add = InetAddress.getByName(address);
        } catch (UnknownHostException e) {
            System.out.println("Could not resolve the address: " + address);
            return false;
        }

        try {
            int portNumber = Integer.parseInt(port);
            socket = new Socket(add, portNumber);

            // Setup input/output streams
            in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
            out = new PrintWriter(new BufferedWriter(new OutputStreamWriter(socket.getOutputStream())), true);

            return true;

        } catch (NumberFormatException e) {
            System.out.println("Port is not a valid number: " + port);
            return false;

        } catch (IOException e) {
            System.out.println("Failed to connect or set up streams: " + e.getMessage());
            if (socket != null && !socket.isClosed()) {
                try {
                    socket.close(); // Ensure socket is closed if an error occurs
                } catch (IOException ex) {
                    System.out.println("Failed to close socket: " + ex.getMessage());
                }
            }
            return false;
        }
}
}
