package V2.Main.FileSharing;

import V2.Auxiliary.MessageTypes.DownloadRelated.FileBlockRequest;
import V2.Auxiliary.MessageTypes.DownloadRelated.FileBlockResult;
import V2.Main.Connection.OpenConnection;

import java.util.ArrayList;
import java.util.List;

public class UploadProcess implements Runnable{
    private List<Integer> indexBuffer = new ArrayList();
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
    }

    @Override
    public void run() {
        while(running){
            while(indexBuffer.isEmpty()){
                try {
                    wait();
                } catch (InterruptedException e) {System.out.println("Processo " + PROCESS_ID + " interrupted");}
            }
            int indexToSend = indexBuffer.getFirst();
            FileBlockResult blockToSend = blocks.get(indexToSend);
            blockToSend.setId(PROCESS_ID);
            connection.sendFileBlockResult(blockToSend);
        }
    }

    public void requestBlock(FileBlockRequest request){
        indexBuffer.add(request.getBlockIndex());
        notifyAll();
    }

    public String getId(){return PROCESS_ID;}
}
