package V2;

import V2.Main.Connection.ConnectionManager;
import V2.Main.Coordinator;
import V2.Main.FileSharing.FileTransferManager;
import V2.Main.Interface.UserInterface;
import V2.Main.Repository.Repo;

public class Init {
    public Init(int id, String filePath) {

        if(id<0) throw new IllegalArgumentException();

        Coordinator controller = Coordinator.getInstance();
        ConnectionManager cm = ConnectionManager.getInstance(8080+id);
        FileTransferManager ftm = FileTransferManager.getInstance(5);
        Repo repo = Repo.getInstance(filePath);
        UserInterface ui = UserInterface.getInstance();
    }


    public static void main(String[] args) {
        Init controller = new Init(0, "dll1");
        Init controller2 = new Init(1, "dll2");
        Init controller3 = new Init(2, "dll3");
    }
}
