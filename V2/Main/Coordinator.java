package V2.Main;

import V2.Auxiliary.DownloadRelated.FileBlockRequest;
import V2.Auxiliary.DownloadRelated.FileBlockResult;
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

        String processId = FileTransferManager.getInstance().startDownloadProcess(fileToDownload);
        ConnectionManager.getInstance().prepareSeeders(processId, fileToDownload);
    }


    // Check for the requested file, if present become a seeder for the request download
    public UploadProcess StartUploadProcess(OpenConnection connectionWithDownloadingPeer, FileBlockRequest request){
        // Check if the file is present in repo, if so calculate blocks
        List<FileBlockResult> blocks = Repo.getInstance().calculateFileBlocks(request.getMetadata(), request.getPreferredBlockSize());
        if(blocks.isEmpty()) return null; // File isn't present in repo

        // Send confirmation to the requesting peer and start an upload process
        UploadProcess process = FileTransferManager.getInstance().startUploadProcess(blocks, connectionWithDownloadingPeer, request.getId());
        return process;
    }


}
