package auxiliary.ConnectionRelated;

import auxiliary.Message;

public class NewConnectionRequest implements Message {
    private final int PORT;


    public NewConnectionRequest(int port) {
        PORT = port;
    }

    public int getPort() {
        return PORT;
    }
}
