package V2.Main;

import V2.Auxiliary.MessageTypes.DownloadRelated.FileBlockResult;
import V2.Auxiliary.MessageTypes.DownloadRelated.FileDownloadRequest;
import V2.Auxiliary.MessageTypes.DownloadRelated.FileDownloadResponse;
import V2.Auxiliary.MessageTypes.SearchRelated.WordSearchRequest;
import V2.Auxiliary.Structs.FileMetadata;
import V2.Main.Connection.ConnectionManager;
import V2.Main.Connection.OpenConnection;
import V2.Main.FileSharing.FileTransferManager;
import V2.Main.FileSharing.UploadProcess;
import V2.Main.Repository.Repo;
import V2.Main.Interface.UserInterface;

import java.util.HashMap;
import java.util.List;
import java.util.PriorityQueue;

public class Controller {
    public final int PORT;
    private final String filename;
    private final ConnectionManager connectionManager;
    private final FileTransferManager fileTransferManager;
    private final Repo repo;
    private final UserInterface userInterface;


    public Controller(int id, String fileName){
        if(id<0) throw new IllegalArgumentException();

        this.filename = fileName;
        this.PORT = 8080 + id;
        this.userInterface = new UserInterface(this);
        connectionManager = new ConnectionManager(this, PORT);
        fileTransferManager = new FileTransferManager(this, 5);
        repo = new Repo(fileName);
    }

/*-------------------------------------------------- Download Related ------------------------------------------------*/

    // Upon download request called from the User Interface a new download process is immediately started and
    // a request is flooded across all connected peers
    public void initiateDownload(FileMetadata fileToDownload) {
        // Create the request so the UUID is generated
        FileDownloadRequest request = new FileDownloadRequest(fileToDownload);
        String processId = request.getId();

        fileTransferManager.startDownloadProcess(processId, fileToDownload);
        connectionManager.floodMessage(request);
    }


    // Check for the requested file, if present become a seeder for the request download
    public void attemptToStartUploadProcess(OpenConnection connectionWithDownloadingPeer, FileDownloadRequest request){
        // Check if the file is present in repo, if so calculate blocks
        List<FileBlockResult> blocks = repo.calculateFileBlocks(request.getFile());
        if(blocks.isEmpty()) return; // File isn't present in repo

        // Send confirmation to the requesting peer and start an upload process
        UploadProcess process = fileTransferManager.startUploadProcess(blocks, connectionWithDownloadingPeer, request.getId());
        connectionManager.sendDownloadResponse(connectionWithDownloadingPeer, request.getId(), process);
    }


    public void addNewSeederToDownloadProcess(FileDownloadResponse fileDownloadResponse, OpenConnection connection) {
        fileTransferManager.addNewSeederToDownloadProcess(fileDownloadResponse, connection);
    }

/*------------------------------------------------- Connection Related -----------------------------------------------*/

    public int requestNewConnection(String address, int port, String keyWord){
        connectionManager.setKeyWord(keyWord);
        return connectionManager.requestConnection(address, port);
    }

    public void filterSearchList(String keyWord){
        connectionManager.setKeyWord(keyWord);
        connectionManager.floodMessage(new WordSearchRequest(keyWord));
    }




/*----------------------------------------------------- File Related -------------------------------------------------*/

    public List<FileMetadata> wordSearchResponse(String keyWord) {
        return repo.wordSearchResponse(keyWord);
    }

    public void refreshRepo() {
        repo.refreshRepo();
    }




    /*------------------------------------------------------ UI ------------------------------------------------------*/
    public void updateUiList(List<FileMetadata> list){
        userInterface.addContentToSearchList(list);
    }






    public static void main(String[] args) {
        Controller controller = new Controller(0, "dll1");
        Controller controller2 = new Controller(1, "dll2");
        Controller controller3 = new Controller(2, "dll3");
    }


    public void delieverFileData(PriorityQueue<FileBlockResult> blocks, FileMetadata fileMetadata,
                                                    HashMap<Integer, Integer> blocksPerSeeder) {
        if(repo.writeFile(blocks, fileMetadata)){
            userInterface.showDownloadInfo(blocksPerSeeder);
        }
    }
}
