import main.Connection.ConnectionManager;
import main.Coordinator;
import main.FileSharing.FileTransferManager;
import main.Interface.UserInterface;
import main.Repository.Repo;

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
