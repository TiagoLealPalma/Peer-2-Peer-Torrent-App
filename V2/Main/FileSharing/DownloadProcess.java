package V2.Main.FileSharing;

import V2.Auxiliary.MessageTypes.DownloadRelated.FileBlockResult;
import V2.Auxiliary.Structs.FileMetadata;
import V2.Main.Connection.OpenConnection;

import java.util.*;

public class DownloadProcess {
    private final String PROCESS_ID;
    private final FileTransferManager manager;
    private final FileMetadata fileMetadata;

    private PriorityQueue<FileBlockResult> blocks;
    private List<DownloadWorker> workers;

    private final int blocksExpected;
    private int currentBlock;
    private int finishedWorkers = 0;

    private HashMap<Integer, Integer> blocksDelieveredPerPort = new HashMap<>();


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
        System.out.println(String.format("New seeder (%d) added to the current download process (%s)",connection.getCorrespondentPort() ,fileMetadata.getFileName()));
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


    public synchronized void addBlocksToQueue(List<FileBlockResult> blocksFromWorker, OpenConnection connection) {
        System.out.println(String.format("(%d) Acabei o meu download e entreguei %d blocos.", connection.getHomePort(), blocksFromWorker.size()));
        blocks.addAll(blocksFromWorker);
        finishedWorkers++;
        blocksDelieveredPerPort.put(connection.getCorrespondentPort(), blocksFromWorker.size());
        if(finishedWorkers == workers.size()) {
            delieverFileData();
            shutdownWorkers();
        }
    }

    private void delieverFileData() {
        manager.deliverFileData(blocks, fileMetadata, blocksDelieveredPerPort);
    }

    private void shutdownWorkers() {

    }
}
