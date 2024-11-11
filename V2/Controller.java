package V2;

import V2.Structs.FileMetadata;

import java.util.ArrayList;
import java.util.List;

public class Controller {
    public final int PORT;
    private String filename;
    private final V2.ConnectionManager connectionManager;
    private V2.DownloadManager downloadManager;
    private V2.Repo repo;
    private V2.UserInterface userInterface;


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

    public boolean requestNewConnection(String address, int port, String keyWord){
        connectionManager.setKeyWord(keyWord);
        return connectionManager.requestConnection(address, port) != null;
    }

    public void filterSearchList(String keyWord){
        connectionManager.setKeyWord(keyWord);
        connectionManager.floodWordSearchRequest(keyWord);
    }




    /*----------------------------------------------------- File -----------------------------------------------------*/

    public List<FileMetadata> wordSearchResponse(String keyWord) {
        return repo.wordSearchResponse(keyWord);
    }




    /*------------------------------------------------------ UI ------------------------------------------------------*/
    // Peers são egoistas e só pedem informação para si, não fornecem informação aos outros, sem esta ser pedida
    // V1.PeerSocket ---> Connection Mannager ---> V1.Controller ---> V1.UserInterface
    public void updateUiList(List<FileMetadata> list){
        userInterface.addContentToSearchList(list);
    }






    public static void main(String[] args) {
        Controller controller = new Controller(0, "dll1");
        Controller controller2 = new Controller(1, "dll2");
        Controller controller3 = new Controller(2, "dll3");
    }
}
