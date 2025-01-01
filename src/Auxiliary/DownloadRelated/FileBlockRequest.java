package src.Auxiliary.DownloadRelated;

import src.Auxiliary.Message;
import src.Auxiliary.Structs.FileMetadata;

public class FileBlockRequest implements Message {
    private final String id;
    private final FileMetadata metadata;
    private final int preferredBlockSize; // used to start the upload on the correct basis
    private final int offset; // Starting byte of the whole file
    private final int length; // used to confirm if the blocks are equal


    public FileBlockRequest(String id, FileMetadata metadata, int offset, int preferedBlockSize, int length) {
        this.offset = offset;
        this.id = id;
        this.metadata = metadata;
        this.preferredBlockSize = preferedBlockSize;
        this.length = length;
    }

    public int getLength(){return length;}
    public int getOffset() {return offset;}
    public String getId() {return id;}
    public FileMetadata getMetadata() {return metadata;}
    public int getPreferredBlockSize() {return preferredBlockSize;}
}

