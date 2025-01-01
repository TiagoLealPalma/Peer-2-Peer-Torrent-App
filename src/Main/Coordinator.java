package src.Main;

import src.Auxiliary.DownloadRelated.FileBlockRequest;
import src.Auxiliary.DownloadRelated.FileBlockResult;
import src.Auxiliary.Structs.FileMetadata;
import src.Main.Connection.ConnectionManager;
import src.Main.Connection.OpenConnection;
import src.Main.FileSharing.FileTransferManager;
import src.Main.FileSharing.UploadProcess;
import src.Main.Repository.Repo;

import java.util.List;

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
        String processId = FileTransferManager.getInstance().startDownloadProcess(fileToDownload);
        ConnectionManager.getInstance().prepareSeeders(processId, fileToDownload);
    }


    // Check for the requested file, if present become a seeder for the request download
    public UploadProcess StartUploadProcess(OpenConnection connectionWithDownloadingPeer, FileBlockRequest request){
        // Check if the file is present in repo, if so calculate blocks
        List<FileBlockResult> blocks = Repo.getInstance().calculateFileBlocks(request.getMetadata(), request.getPreferredBlockSize());
        if(blocks.isEmpty()) return null; // File isn't present in repo

        // Send confirmation to the requesting peer and start an upload process
        return FileTransferManager.getInstance().startUploadProcess(blocks, connectionWithDownloadingPeer, request.getId());
    }


}
