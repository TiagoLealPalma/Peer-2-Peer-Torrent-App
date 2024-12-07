package V2.Main.FileSharing;

import V2.Auxiliary.DownloadRelated.FileBlockRequest;
import V2.Auxiliary.DownloadRelated.FileBlockResult;
import V2.Main.Connection.OpenConnection;

import java.util.ArrayList;
import java.util.List;

public class UploadProcess implements Runnable{
    private List<FileBlockRequest> requestBuffer = new ArrayList();
    private List<FileBlockResult> blocks;
    private final String PROCESS_ID;
    private final FileTransferManager manager;
    private final OpenConnection connection;

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
        while(!blocks.isEmpty()){
            while(requestBuffer.isEmpty()){
                synchronized (this){
                    try {
                        wait();
                    } catch (InterruptedException e) {System.out.println("Processo " + PROCESS_ID + " interrupted");}
                }
            }
            FileBlockRequest request = requestBuffer.removeFirst();
            FileBlockResult result = null;

            for (FileBlockResult block : blocks) {
                if(block.equals(request)) result = block;
            }
            if(result == null) System.err.println("Erro occurreu a tentar enviar o bloco com offset " + request.getOffset());
            // If not, send the block response normally
            connection.sendMessage(result);
        }
    }

    public synchronized void requestBlock(FileBlockRequest request){
        requestBuffer.add(request);
        System.out.println(String.format("Bloco %d entregue", request.getOffset()));
        notifyAll();
    }

    public String getId(){return PROCESS_ID;}
}
