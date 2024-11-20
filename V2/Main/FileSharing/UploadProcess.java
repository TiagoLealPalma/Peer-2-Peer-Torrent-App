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
        this.blocks = blocks;
    }

    // Handles FileBlock requests
    @Override
    public void run() {
        while(running){
            // Checks for pending File Block Requests
            while(indexBuffer.isEmpty()){
                synchronized (this){
                    try {
                        wait(); // Waits until notified for requests
                    } catch (InterruptedException e) {System.out.println("Processo de Upload (" + PROCESS_ID + ") interrompido");}
                }
            }
            // When notified, gets the requested index, searches for it in the FileBlock list
            // and delievers it to the connetion
            int indexToSend = indexBuffer.removeFirst();
            if(indexToSend != -1) {
                FileBlockResult blockToSend = blocks.get(indexToSend);
                blockToSend.setId(PROCESS_ID);
                connection.sendFileBlockResult(blockToSend);
            }
            else{ // Se foi recebido um pedido de bloco "-1", sinaliza que todos os blocos necessários
                // já foram pedidos e que os recursos deste processo podem ser libertados
                System.out.println(String.format("(%d)Processo de upload (%s) concluido.", connection.getHomePort(), PROCESS_ID));
                break;
            }
        }
    }

    // Called from the connection requesting a FileBlockResult send
    public synchronized void requestBlock(FileBlockRequest request){
        indexBuffer.add(request.getBlockIndex());
        notifyAll(); // Notifies the waiting process
    }

    public String getId(){return PROCESS_ID;}
}
