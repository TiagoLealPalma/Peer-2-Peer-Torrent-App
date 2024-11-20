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


/*-------------------------------------------------- File Transfer Related ------------------------------------------------*/
    // Upon download request called from the User Interface a new download process is immediately started and
    // a request is flooded across all connected peers
    public void initiateDownload(FileMetadata fileToDownload) {
        // Create the request so the UUID is generated
        FileDownloadRequest request = new FileDownloadRequest(fileToDownload);
        String processId = request.getId();

        fileTransferManager.startDownloadProcess(processId, fileToDownload);
        connectionManager.floodMessage(request);
    }


    // Checks for the requested file, if present become a seeder for the request download
    public void attemptToStartUploadProcess(OpenConnection connectionWithDownloadingPeer, FileDownloadRequest request){
        // Check if the file is present in repo, if so calculate blocks
        List<FileBlockResult> blocks = repo.calculateFileBlocks(request.getFile());
        if(blocks.isEmpty()) return; // File isn't present in repo

        // Send confirmation to the requesting peer and start an upload process
        UploadProcess process = fileTransferManager.startUploadProcess(blocks, connectionWithDownloadingPeer, request.getId());
        connectionManager.sendDownloadResponse(connectionWithDownloadingPeer, request.getId(), process);
    }


    // Called from the connection manager when a peer shows availability to be a seeder for a given download
    public void addNewSeederToDownloadProcess(FileDownloadResponse fileDownloadResponse, OpenConnection connection) {
        fileTransferManager.addNewSeederToDownloadProcess(fileDownloadResponse, connection);
    }


    // After the download is finished, FileTransferManager calls this method which takes care of passing the data
    // to the Repo class in order for it to be written and handles in case of error
    public void deliverFileData(PriorityQueue<FileBlockResult> blocks, FileMetadata fileMetadata,
                                HashMap<Integer, Integer> blocksPerSeeder) {
        // Trys to write file in directory
        if (repo.writeFile(blocks, fileMetadata)) {
            userInterface.showDownloadInfo(blocksPerSeeder);
        } else { // Error occurred while trying to write a file
            userInterface.popUpPrint(String.format("Ocorreu um erro na escrita do ficheiro (%s)", fileMetadata.getFileName()));
        }
    }


/*------------------------------------------------- Connection Related -----------------------------------------------*/
    // Called from the GUI when the user wants to connect to a peer and passes down the connection task to
    // the connection manager.
    public int requestNewConnection(String address, int port, String keyWord){
        connectionManager.setKeyWord(keyWord);
        return connectionManager.requestConnection(address, port);
    }


    // Called from the GUI when the user wants to filter the displayed files and asks the connection manager
    // to flood its know peers network with WordSearchRequests with the keyword.
    public void filterSearchList(String keyWord){
        connectionManager.setKeyWord(keyWord);
        connectionManager.floodMessage(new WordSearchRequest(keyWord));
    }


/*----------------------------------------------------- File Related -------------------------------------------------*/
    // Called from the connection manager asking for the list files matching the keyword
    public List<FileMetadata> wordSearchResponse(String keyWord) {
        return repo.wordSearchResponse(keyWord);
    }

    // Called from the GUI whenever the user wants its own directory to be updated.
    // Likely wants to offer new files
    public void refreshRepo() {
        repo.refreshRepo();
    }


/*---------------------------------------------------------- UI ------------------------------------------------------*/
    // Called from the connection manager whenever it receives a WordSearchResult messages and has data that needs to
    // be displayed as consequence of the filter action called by the user
    public void updateUiList(List<FileMetadata> list){
        userInterface.addContentToSearchList(list);
    }


/*--------------------------------------------------------- Main -----------------------------------------------------*/
    public static void main(String[] args) {
        Controller controller = new Controller(0, "dll1");
        Controller controller2 = new Controller(1, "dll2");
        Controller controller3 = new Controller(2, "dll3");
    }



}

