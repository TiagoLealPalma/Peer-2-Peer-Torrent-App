package V2.Auxiliary.ConnectionRelated;

import V2.Auxiliary.Message;

public class NewConnectionRequest implements Message {
    private final int PORT;


    public NewConnectionRequest(int port) {
        PORT = port;
    }

    public int getPort() {
        return PORT;
    }
}
