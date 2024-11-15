package V2.Main.FileSharing;

import V2.Auxiliary.MessageTypes.DownloadRelated.FileBlockResult;
import V2.Auxiliary.Structs.FileMetadata;
import V2.Main.Connection.OpenConnection;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.PriorityQueue;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class DownloadProcess {
    private final String PROCESS_ID;
    private final FileTransferManager manager;
    private final FileMetadata fileMetadata;
    private PriorityQueue<FileBlockResult> blocks;
    private List<DownloadWorker> workers;
    private final int blocksExpected;
    private int currentBlock;
    private int finishedWorkers = 0;


    public DownloadProcess(FileTransferManager downloadManager, FileMetadata fileToDownload,
                                                                                String processId) {
        this.PROCESS_ID = processId;
        this.manager = downloadManager;
        this.fileMetadata = fileToDownload;

        blocksExpected = fileToDownload.getLength();
        blocks = new PriorityQueue<>();
        workers = new ArrayList<>();

    }

    public void addWorker(OpenConnection connection) {
        DownloadWorker worker = new DownloadWorker(this, connection, PROCESS_ID);
        workers.add(worker);
        worker.start();
    }

    // Workers get the index number of the next block they will ask for
    public synchronized int getNextIndex(){
        if(currentBlock >= blocksExpected)
            return -1; // Signalling all blocks have been ask for, and writing process should start

        return currentBlock++;
    }

    public synchronized void addBlocksToQueue(List<FileBlockResult> blocksFromWorker) {
        blocks.addAll(blocksFromWorker);
        finishedWorkers++;
        if(finishedWorkers == workers.size()) {
            delieverFileData();
            shutdownWorkers();
        }
    }

    private void delieverFileData() {
        manager.delieverFileData(blocks, fileMetadata);
    }

    private void shutdownWorkers() {

    }
}
