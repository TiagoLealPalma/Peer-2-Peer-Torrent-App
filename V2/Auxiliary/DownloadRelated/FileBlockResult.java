package V2.Auxiliary.DownloadRelated;

import V2.Auxiliary.Message;

public class FileBlockResult implements Message, Comparable<FileBlockResult> {
    private byte[] block;
    private int blockSize;
    private String id;
    private int index;


    public FileBlockResult(byte[] block, String id, int index) {
        this.block = block;
        this.id = id;
        this.blockSize = block.length;
    }

    public FileBlockResult(byte[] block, int index) {
        this.block = block;
        this.blockSize = block.length;
        this.index = index;
    }

    public int getIndex() {return index;}
    public byte[] getBlock() {return block;}
    public String getId() {return id;}
    public int getBlockSize() {return blockSize;}
    public void setId(String id) {this.id = id;}

    @Override
    public int compareTo(FileBlockResult o) {
        return this.getIndex() - o.getIndex();
    }
}
