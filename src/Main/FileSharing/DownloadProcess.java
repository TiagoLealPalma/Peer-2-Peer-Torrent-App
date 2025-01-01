package src.Main.FileSharing;

import src.Auxiliary.DownloadRelated.FileBlockRequest;
import src.Auxiliary.Structs.FileMetadata;
import src.Main.Connection.OpenConnection;

import java.util.ArrayList;
import java.util.List;

public class DownloadProcess {
    public final String PROCESS_ID;
    private final FileTransferManager manager;
    private final FileMetadata fileMetadata;
    private List<DownloadWorker> workers;
    private List<FileBlockRequest> requests;
    private int runningWorkers = 0;
    private FileWriter writer;


    public DownloadProcess(FileTransferManager downloadManager, FileMetadata fileToDownload,
                                                                                int preferedSizeOfBlock, String processId) {
        this.PROCESS_ID = processId;
        this.manager = downloadManager;
        this.fileMetadata = fileToDownload;

        workers = new ArrayList<>();
        requests = new ArrayList<>();

        // Calcular lista fileBlockRequests
        int bytesLeft = fileToDownload.getLength();
        int blocksTotalSize = 0;
        System.out.println(fileToDownload.getLength());

        while(bytesLeft > 0) {
            int diferenceToZero = 0;
            if((bytesLeft -= preferedSizeOfBlock) < 0) diferenceToZero = bytesLeft;
            requests.add(new FileBlockRequest( PROCESS_ID, fileMetadata, blocksTotalSize, preferedSizeOfBlock,preferedSizeOfBlock + diferenceToZero));
            System.out.println("Block " + blocksTotalSize + ": bytesRead = " + preferedSizeOfBlock + diferenceToZero);
            blocksTotalSize += preferedSizeOfBlock;

        }
        System.out.println("lista:" + requests.size());

        // Writer
        writer = new FileWriter(this, requests.size(), fileMetadata);
    }

    public void addWorker(OpenConnection connection) {
        workers.add(new DownloadWorker(this, connection, writer, PROCESS_ID));
    }

    // Workers get the next block they will ask for or if there are no blocks left, return a block signaling end of process
    public synchronized FileBlockRequest getNextRequest(){
        return requests.isEmpty()? new FileBlockRequest(PROCESS_ID, fileMetadata, -1, -1, -1) : requests.removeFirst();
    }

    public synchronized void returnFailedRequest(FileBlockRequest request){
        requests.add(request);
    }

}
