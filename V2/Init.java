package V2;

import V2.Main.Connection.ConnectionManager;
import V2.Main.Coordinator;
import V2.Main.FileSharing.FileTransferManager;
import V2.Main.Interface.UserInterface;
import V2.Main.Repository.Repo;

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
