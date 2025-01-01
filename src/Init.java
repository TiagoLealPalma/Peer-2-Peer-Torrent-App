package src;

import src.Main.Connection.ConnectionManager;
import src.Main.Coordinator;
import src.Main.FileSharing.FileTransferManager;
import src.Main.Interface.UserInterface;
import src.Main.Repository.Repo;

public class Init {
    public Init(int port, String filePath) {

        if(port<8080) throw new IllegalArgumentException();

        Coordinator controller = Coordinator.getInstance();
        ConnectionManager cm = ConnectionManager.getInstance(port);
        FileTransferManager ftm = FileTransferManager.getInstance(5);
        Repo repo = Repo.getInstance(filePath);
        UserInterface ui = UserInterface.getInstance();
    }
}
