package V2.Auxiliary.MessageTypes.ConnectionRelated;

import V2.Auxiliary.MessageTypes.Message;

public class NewConnectionRequest implements Message {
    private final int PORT;


    public NewConnectionRequest(int port) {
        PORT = port;
    }

    public int getPort() {
        return PORT;
    }
}
