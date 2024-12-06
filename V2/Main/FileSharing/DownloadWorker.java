package V2.Main.FileSharing;

import V2.Auxiliary.DownloadRelated.FileBlockRequest;
import V2.Auxiliary.DownloadRelated.FileBlockResult;
import V2.Auxiliary.Structs.FileMetadata;
import V2.Main.Connection.OpenConnection;

import java.util.ArrayList;
import java.util.List;

// One per seeding peer for a given download process
public class DownloadWorker extends Thread {
    private final DownloadProcess downloadProcess;
    private final OpenConnection connection;
    private List<FileBlockResult> blocks;
    private final String PROCESS_ID;
    private boolean running = true;

    public DownloadWorker(DownloadProcess downloadProcess, OpenConnection connection, String processId) {
        this.downloadProcess = downloadProcess;
        this.connection = connection;
        PROCESS_ID = processId;
        this.blocks = new ArrayList<>();
        connection.connectDownloadWorker(processId, this);
        start();
    }

    @Override
    public void run() {
        while(running){
            // Get the next needed index
            FileBlockRequest currentRequest = downloadProcess.getNextRequest();

            // If there are still blocks left to ask for
            if(currentRequest != null) {
                // Ask for block
                connection.sendMessage(currentRequest);
                System.out.println(String.format("Pedi o bloco %d ao %d", currentRequest.getBlockIndex(), connection.getCorrespondentPort()));
                   synchronized (this) {
                       try {
                           wait(); // Wait for the arrival of the block
                       } catch (InterruptedException e) {
                           System.out.println("Worker (" + PROCESS_ID + ") interrupted");
                       }
                   }
            // If there are no blocks left to ask for
            } else {
                running = false;
                downloadProcess.addBlocksToQueue(blocks, this); //Entregar diretamente ao writer assim que recebe um bloco
            }
        }
    }


    // Called by the connection to submit the received block
    public synchronized void submitFileBlockResult(FileBlockResult fileBlockResult){
        blocks.add(fileBlockResult); // Add block to the temporary list
        System.out.println(String.format("Submitting file block result: %d", fileBlockResult.getIndex()));
        notifyAll(); // Notify its arrival
    }

    public OpenConnection getConnection() {
        return connection;
    }
}
