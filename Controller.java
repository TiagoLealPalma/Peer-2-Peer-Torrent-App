import java.io.File;
import java.util.ArrayList;

public class Controller {
    public final int PORT;
    private String filename;
    private final ConnectionManager connectionManager;
    private DownloadManager downloadManager;
    private Repo repo;
    private UserInterface userInterface;


    public Controller(int id, String fileName){
        if(id<0) throw new IllegalArgumentException();

        this.filename = fileName;
        this.PORT = 8080 + id;
        this.userInterface = new UserInterface(this);
        connectionManager = new ConnectionManager(this, PORT);
        downloadManager = new DownloadManager();
        repo = new Repo(fileName);
    }

    /*--------------------------------------------- Connection Related -----------------------------------------------*/

    public boolean requestNewConnection(String address, String port){
        return connectionManager.requestConnection(address, port);
    }




    /*----------------------------------------------------- File -----------------------------------------------------*/

   public ArrayList<String> getFilesInDirectory(String content){
       return repo.getSearchContent();
   }




    /*----------------------------------------------------- Main -----------------------------------------------------*/
   // Peers são egoistas e só pedem informação para si, não fornecem informação aos outros, sem esta ser pedida
    // PeerSocket ---> Connection Mannager ---> Controller ---> UserInterface
    public void updateSearchList(String[] searchList){
        userInterface.addContentToSearchList(searchList);
    }

    // PeerSocket ---> ConnectionMannager ---> Controller ---> Repo ---> Controller ---> ConnectionManager ---> PeerSocket
    public ArrayList<String> getSearchContent() {
        return repo.getSearchContent();
    }


    public static void main(String[] args) {
        Controller controller = new Controller(0, "dll1");
        Controller controller2 = new Controller(1, "dll2");
        Controller controller3 = new Controller(2, "dll3");
    }


}
