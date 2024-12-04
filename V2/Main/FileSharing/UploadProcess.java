package V2.Main.FileSharing;

import V2.Auxiliary.DownloadRelated.FileBlockRequest;
import V2.Auxiliary.DownloadRelated.FileBlockResult;
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
        this.blocks = blocks;
    }

    @Override
    public void run() {
        while(running){
            while(indexBuffer.isEmpty()){
                synchronized (this){
                    try {
                        wait();
                    } catch (InterruptedException e) {System.out.println("Processo " + PROCESS_ID + " interrupted");}
                }
            }
            int indexToSend = indexBuffer.removeFirst();
            FileBlockResult blockToSend = blocks.get(indexToSend);
            blockToSend.setId(PROCESS_ID);
            connection.sendFileBlockResult(blockToSend);
        }
    }

    public synchronized void requestBlock(FileBlockRequest request){
        indexBuffer.add(request.getBlockIndex());
        System.out.println(String.format("Bloco %d entregue", request.getBlockIndex()));
        notifyAll();
    }

    public String getId(){return PROCESS_ID;}
}
