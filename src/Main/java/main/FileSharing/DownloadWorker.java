package main.FileSharing;

import auxiliary.DownloadRelated.FileBlockRequest;
import auxiliary.DownloadRelated.FileBlockResult;
import main.Connection.OpenConnection;

// One per seeding peer for a given download process
public class DownloadWorker extends Thread {
    private final DownloadProcess downloadProcess;
    private final OpenConnection connection;
    private final FileWriter writer;
    private final String PROCESS_ID;
    private boolean waitingForResponse = false;
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
            if(currentRequest.getOffset() != -1){
                // Send request and set waiting flag
                waitingForResponse = true;
                connection.sendMessage(currentRequest);

                    // Wait for block
                   synchronized (this) {
                       try {
                           wait(200); // Wait for the arrival of the block
                       } catch (InterruptedException e) {
                           System.out.println("Worker (" + PROCESS_ID + ") interrupted");
                       }
                   }

                   // If wait is over and still waiting for response, return the block so another worker has a chance to get it
                   if(waitingForResponse){
                       handleRequestTimeout(currentRequest);
                   }

            // If there are no blocks left to ask for
            } else {
                // Let seeders know process is finished
                connection.sendMessage(currentRequest);
                running = false;
            }
        }
    }


    // Called by the connection to submit the received block
    public synchronized void submitFileBlockResult(FileBlockResult fileBlockResult){
        waitingForResponse = false;
        writer.putBlock(fileBlockResult, this); // Add block to writer
        notifyAll(); // Notify its arrival
    }

    // Handles the block not being received
    private void handleRequestTimeout(FileBlockRequest currentRequest) {
        downloadProcess.returnFailedRequest(currentRequest);
        connection.consoleLog(String.format("Request (Block %d) timed out after 5 second wait", currentRequest.getOffset()));
        waitingForResponse = false;
    }

    public OpenConnection getConnection() {
        return connection;
    }
}



//System.out.println(String.format("Pedi o bloco %d ao %d", currentRequest.getOffset(), connection.getCorrespondentPort()));
//System.out.println(String.format("Submitting file block result: %d", fileBlockResult.getOffset()));