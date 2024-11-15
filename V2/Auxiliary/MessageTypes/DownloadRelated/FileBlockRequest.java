package V2.Auxiliary.MessageTypes.DownloadRelated;

import V2.Auxiliary.MessageTypes.Message;

public class FileBlockRequest implements Message {
    private int blockIndex;
    private String id;


    public FileBlockRequest(int blockIndex, String id) {
        this.blockIndex = blockIndex;
        this.id = id;
    }

    public int getBlockIndex() {return blockIndex;}
    public String getId() {return id;}
}

