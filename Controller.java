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

    public void requestNewConnection(String address, String port){
        int maxTries = 5;
        boolean connected = false;

        while(!connected && maxTries-- > 0){
            ConnectionManager connectionManager = new ConnectionManager();
            connectionManager.connectToPeer(address, port);
        }
    }
}
