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
    private final FileWriter writer;
    private List<FileBlockResult> blocks;
    private final String PROCESS_ID;
    private boolean running = true;

    public DownloadWorker(DownloadProcess downloadProcess, OpenConnection connection, FileWriter writer, String processId) {
        this.downloadProcess = downloadProcess;
        this.connection = connection;
        PROCESS_ID = processId;
        this.writer = writer;
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
                System.out.println(String.format("Pedi o bloco %d ao %d", currentRequest.getOffset(), connection.getCorrespondentPort()));
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
            }
        }
    }


    // Called by the connection to submit the received block
    public synchronized void submitFileBlockResult(FileBlockResult fileBlockResult){

        writer.putBlock(fileBlockResult, this); // Add block to writer
        System.out.println(String.format("Submitting file block result: %d", fileBlockResult.getOffset()));
        notifyAll(); // Notify its arrival
    }

    public OpenConnection getConnection() {
        return connection;
    }
}
