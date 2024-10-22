import java.io.*;
import java.net.InetAddress;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.Scanner;

public class ConnectionManager {

    private BufferedReader in;
    private PrintWriter out;
    private ArrayList<ClientSocket> clientSockets = new ArrayList();
    private ArrayList<ServerSocket> serverSockets = new ArrayList();

    public void startServing(){



    }

    public boolean connectToPeer(String address, String port) {
        return  true;
    }


    private boolean sendMessages() {
        return true;
    }

}
