package auxiliary.DownloadRelated;

import auxiliary.Message;

public class FileBlockResult implements Message, Comparable<FileBlockResult> {
    private byte[] block;
    private String id;
    private final int offset;
    private final int length;


    public FileBlockResult(byte[] block, String id, int offset) {
        this.block = block;
        this.id = id;
        this.length = block.length;
        this.offset = offset;
    }

    public FileBlockResult(byte[] block, int offset) {
        this.block = block;
        this.length = block.length;
        this.offset = offset;
    }

    @Override
    public boolean equals(Object obj) {
        if(obj instanceof FileBlockRequest request)
            return this.id.equals(request.getId()) && this.offset == request.getOffset() && this.length == request.getLength();
        return super.equals(obj);
    }

    public int getOffset() {return offset;}
    public int getLength() {return length;}
    public byte[] getBlock() {return block;}
    public String getId() {return id;}
    public void setId(String id) {this.id = id;}

    @Override
    public int compareTo(FileBlockResult o) {
        return this.getOffset() - o.getOffset();
    }
}
