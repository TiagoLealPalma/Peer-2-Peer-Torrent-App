public class Controller {
    public final int PORT;
    private final ConnectionManager connectionManager;
    private DownloadManager downloadManager;
    private Repo repo;
    private UserInterface userInterface;

    public Controller(int id){
        if(id<0) throw new IllegalArgumentException();

        this.PORT = 8080 + id;
        this.userInterface = new UserInterface(this);
        connectionManager = new ConnectionManager(PORT);
        downloadManager = new DownloadManager();
        repo = new Repo();
    }

    /*--------------------------------------------- Connection Related -----------------------------------------------*/

    public boolean requestNewConnection(String address, String port){
        return connectionManager.requestConnection(address, port);
    }




    /*----------------------------------------------------- Main -----------------------------------------------------*/
    public static void main(String[] args) {
        Controller controller = new Controller(0);
        Controller controller2 = new Controller(1);
    }
}
