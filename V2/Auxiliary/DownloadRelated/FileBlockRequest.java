package V2.Auxiliary.DownloadRelated;

import V2.Auxiliary.Message;
import V2.Auxiliary.Structs.FileMetadata;

public class FileBlockRequest implements Message {
    private String id;
    private FileMetadata metadata;
    private final int preferedBlockSize;
    private int blockIndex; // aplicar isto na escrita dos ficheiros


    public FileBlockRequest(int blockIndex, String id, FileMetadata metadata, int preferedBlockSize) {
        this.blockIndex = blockIndex;
        this.id = id;
        this.metadata = metadata;
        this.preferedBlockSize = preferedBlockSize;
    }

    public int getBlockIndex() {return blockIndex;}
    public String getId() {return id;}
    public FileMetadata getMetadata() {return metadata;}
    public int getPreferedBlockSize() {return preferedBlockSize;}
}

