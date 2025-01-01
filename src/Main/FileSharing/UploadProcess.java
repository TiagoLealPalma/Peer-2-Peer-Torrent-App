package src.Main.FileSharing;

import src.Auxiliary.DownloadRelated.FileBlockRequest;
import src.Auxiliary.DownloadRelated.FileBlockResult;
import src.Main.Connection.OpenConnection;

import java.util.ArrayList;
import java.util.List;

public class UploadProcess implements Runnable{
    private List<FileBlockRequest> requestBuffer = new ArrayList();
    private List<FileBlockResult> blocks;
    private final String PROCESS_ID;
    private final FileTransferManager manager;
    private final OpenConnection connection;
    private boolean running = true;

    public UploadProcess(OpenConnection connection, String processID,
                         FileTransferManager manager, List<FileBlockResult> blocks) {
        this.PROCESS_ID = processID;
        this.manager = manager;
        this.connection = connection;
        this.blocks = blocks;
        System.out.println("Upload: " + blocks.size());

        // Atribuir Ids
        for (FileBlockResult block : blocks) {
            block.setId(processID);
        }
    }

    @Override
    public void run() {
        while(running){
            // Wait for requests
            while(requestBuffer.isEmpty()){
                synchronized (this){
                    try {
                        wait();
                    } catch (InterruptedException e) {System.out.println("Processo " + PROCESS_ID + " interrupted");}
                }
            }

            // Handle requests
            FileBlockRequest request = requestBuffer.removeFirst();

            // If process is not finished and request is valid
            if(request.getOffset() != -1) {
                FileBlockResult result = null;

                // Search for corresponding block
                for (FileBlockResult block : blocks) {
                    if (block.equals(request)) result = block;
                }

                // Check if found
                if (result != null)
                    connection.sendMessage(result);
                else
                    System.err.println("Erro occurreu a tentar enviar o bloco com offset " + request.getOffset());

            // Process is finished
            }else{
                running = false;
            }
        }
    }

    public synchronized void requestBlock(FileBlockRequest request){
        requestBuffer.add(request);
        System.out.println(String.format("Bloco %d entregue", request.getOffset()));
        notifyAll();
    }

    public String getId(){return PROCESS_ID;}
}
