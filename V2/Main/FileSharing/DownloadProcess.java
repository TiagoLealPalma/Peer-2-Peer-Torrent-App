package V2.Main.FileSharing;

import V2.Auxiliary.DownloadRelated.FileBlockRequest;
import V2.Auxiliary.DownloadRelated.FileBlockResult;
import V2.Auxiliary.Structs.FileMetadata;
import V2.Main.Connection.OpenConnection;

import java.util.ArrayList;
import java.util.List;
import java.util.PriorityQueue;

public class DownloadProcess {
    private final String PROCESS_ID;
    private final FileTransferManager manager;
    private final FileMetadata fileMetadata;
    private List<DownloadWorker> workers;
    private List<FileBlockRequest> requests;
    private int runningWorkers = 0;
    private String workersBlocksReceived = "";
    private long startTime;
    private FileWriter writer;


    public DownloadProcess(FileTransferManager downloadManager, FileMetadata fileToDownload,
                                                                                int preferedSizeOfBlock, String processId) {
        this.PROCESS_ID = processId;
        this.manager = downloadManager;
        this.fileMetadata = fileToDownload;

        startTime = System.currentTimeMillis();

        workers = new ArrayList<>();
        requests = new ArrayList<>();

        // Calcular lista fileBlockRequests
        int bytesLeft = fileToDownload.getLength();
        int blocksTotalSize = 0;

        while(bytesLeft < 0) {
            int diferenceToZero = 0;
            if((bytesLeft -= preferedSizeOfBlock) < 0) diferenceToZero = bytesLeft;
            requests.add(new FileBlockRequest(blocksTotalSize, PROCESS_ID, fileMetadata, preferedSizeOfBlock + diferenceToZero));
            blocksTotalSize += preferedSizeOfBlock;
        }
        System.out.println("lista:" + requests.size());

        // Writer
        writer = new FileWriter(this, requests.size());
    }

    public void addWorker(OpenConnection connection) {
        runningWorkers++;
        workers.add(new DownloadWorker(this, connection, PROCESS_ID));

    }

    // Workers get the index number of the next block they will ask for
    public synchronized FileBlockRequest getNextRequest(){
        if(!requests.isEmpty()){
            return requests.removeFirst();
        }
        return null;
    }

    public synchronized void addBlocksToQueue(List<FileBlockResult> blocksFromWorker, DownloadWorker worker) {
        runningWorkers--;
        workersBlocksReceived += String.format("Blocos recebido do peer %d: %d \n", worker.getConnection().getCorrespondentPort(), blocksFromWorker.size());

        for(FileBlockResult block : blocksFromWorker){
            blocks.add(block);
        }

        if(runningWorkers == 0) {
            delieverFileData();
        }
    }

    private void delieverFileData() {
        manager.delieverFileData(blocks, fileMetadata, workersBlocksReceived + "Tempo de Download: " +
                ((long)(startTime - System.currentTimeMillis())/1000)  + " segundos");
    }
}
