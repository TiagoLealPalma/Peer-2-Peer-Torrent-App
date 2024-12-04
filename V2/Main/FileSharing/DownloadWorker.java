package V2.Main.FileSharing;

import V2.Auxiliary.DownloadRelated.FileBlockRequest;
import V2.Auxiliary.DownloadRelated.FileBlockResult;
import V2.Main.Connection.OpenConnection;

import java.util.ArrayList;
import java.util.List;

// One per seeding peer for a given download process
public class DownloadWorker extends Thread {
    private final DownloadProcess downloadProcess;
    private final OpenConnection connection;
    private List<FileBlockResult> blocks;
    private final String PROCESS_ID;
    private int currentIndex = 0;
    private boolean running = true;

    public DownloadWorker(DownloadProcess downloadProcess, OpenConnection connection, String processId) {
        this.downloadProcess = downloadProcess;
        this.connection = connection;
        PROCESS_ID = processId;
        this.blocks = new ArrayList<>();

    }

    @Override
    public void run() {
        while(running){
            // Get the next needed index
            currentIndex = downloadProcess.getNextIndex();

            // If there are still blocks left to ask for
            if(currentIndex != -1) {
                // Ask for block
                connection.sendFileBlockRequest(new FileBlockRequest(currentIndex, PROCESS_ID), this);
                   synchronized (this) {
                       try {
                           wait(); // Wait for the arrival of the block
                       } catch (InterruptedException e) {
                           System.out.println("Worker (" + PROCESS_ID + ") interrupted");
                       }
                   }
            // If there are no blocks left to ask for
            } else {
                downloadProcess.addBlocksToQueue(blocks);
            }
        }
    }


    // Called by the connection to submit the received block
    public synchronized void submitFileBlockResult(FileBlockResult fileBlockResult){
        blocks.add(fileBlockResult); // Add block to the temporary list
        System.out.println(String.format("Submitting file block result: %d", currentIndex));
        notifyAll(); // Notify its arrival
    }
}
