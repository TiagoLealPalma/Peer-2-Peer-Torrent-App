package V2.Main;

import V2.Auxiliary.DownloadRelated.FileBlockResult;
import V2.Auxiliary.DownloadRelated.FileDownloadRequest;
import V2.Auxiliary.DownloadRelated.FileDownloadResponse;
import V2.Auxiliary.SearchRelated.WordSearchRequest;
import V2.Auxiliary.Structs.FileMetadata;
import V2.Main.Connection.ConnectionManager;
import V2.Main.Connection.OpenConnection;
import V2.Main.FileSharing.FileTransferManager;
import V2.Main.FileSharing.UploadProcess;
import V2.Main.Interface.UserInterface;
import V2.Main.Repository.Repo;

import java.util.List;
import java.util.PriorityQueue;

public class Coordinator {
    // Helps with coordinating tasks that involve multiple main classes
    private static Coordinator instance;

    // Prevent reflection-based instantiation
    public static synchronized Coordinator getInstance() {
        if (instance == null) {
            instance = new Coordinator();
        }
        return instance;
    }

/*-------------------------------------------------- Download Related ------------------------------------------------*/

    // Upon download request called from the User Interface a new download process is immediately started and
    // a request is flooded across all connected peers
    public void initiateDownload(FileMetadata fileToDownload) {
        // Create the request so the UUID is generated
        FileDownloadRequest request = new FileDownloadRequest(fileToDownload);
        String processId = request.getId();

        FileTransferManager.getInstance().startDownloadProcess(processId, fileToDownload);
        ConnectionManager.getInstance().floodMessage(request);
    }


    // Check for the requested file, if present become a seeder for the request download
    public void attemptToStartUploadProcess(OpenConnection connectionWithDownloadingPeer, FileDownloadRequest request){
        // Check if the file is present in repo, if so calculate blocks
        List<FileBlockResult> blocks = Repo.getInstance().calculateFileBlocks(request.getFile());
        if(blocks.isEmpty()) return; // File isn't present in repo

        // Send confirmation to the requesting peer and start an upload process
        UploadProcess process = FileTransferManager.getInstance().startUploadProcess(blocks, connectionWithDownloadingPeer, request.getId());
        ConnectionManager.getInstance().sendDownloadResponse(connectionWithDownloadingPeer, request.getId(), process);
    }


    public void addNewSeederToDownloadProcess(FileDownloadResponse fileDownloadResponse, OpenConnection connection) {
        FileTransferManager.getInstance().addNewSeederToDownloadProcess(fileDownloadResponse, connection);
    }

/*------------------------------------------------- Connection Related -----------------------------------------------*/

    public int requestNewConnection(String address, int port, String keyWord){
        ConnectionManager.getInstance().setKeyWord(keyWord);
        return ConnectionManager.getInstance().requestConnection(address, port);
    }

    public void filterSearchList(String keyWord){
        ConnectionManager.getInstance().setKeyWord(keyWord);
        ConnectionManager.getInstance().floodMessage(new WordSearchRequest(keyWord));
    }




/*----------------------------------------------------- File Related -------------------------------------------------*/

    public List<FileMetadata> wordSearchResponse(String keyWord) {
        return Repo.getInstance().wordSearchResponse(keyWord);
    }

    public void refreshRepo() {
        Repo.getInstance().refreshRepo();
    }




/*---------------------------------------------------------- UI ------------------------------------------------------*/
    public void updateUiList(List<FileMetadata> list){
        UserInterface.getInstance().addContentToSearchList(list);
    }

    public void delieverFileData(PriorityQueue<FileBlockResult> blocks, FileMetadata fileMetadata) {
        if(Repo.getInstance().writeFile(blocks, fileMetadata)){
            UserInterface.getInstance().showDownloadInfo();
        }
    }


}
