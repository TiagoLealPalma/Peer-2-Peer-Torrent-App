public class Controller {
    private ConnectionManager connectionManager;
    private DownloadManager downloadManager;
    private Repo repo;
    private UserInterface userInterface;

    public Controller(UserInterface userInterface){
        this.userInterface = userInterface;
        connectionManager = new ConnectionManager();
        downloadManager = new DownloadManager();
        repo = new Repo();
    }

    public void setupConnection(String address, String port){

    }
}
