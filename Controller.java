public class Controller {
    private final ConnectionManager connectionManager;
    private DownloadManager downloadManager;
    private Repo repo;
    private UserInterface userInterface;

    public Controller(int id){
        this.userInterface = new UserInterface(this);
        connectionManager = new ConnectionManager(8080+id);
        downloadManager = new DownloadManager();
        repo = new Repo();
    }

    /*--------------------------------------------- Connection Related -----------------------------------------------*/

    public boolean requestNewConnection(String address, String port){
        return connectionManager.requestConnection(address, port);
    }




    /*----------------------------------------------------- Main -----------------------------------------------------*/
    public static void main(String[] args) {
        Controller controller = new Controller(1);
    }
}
