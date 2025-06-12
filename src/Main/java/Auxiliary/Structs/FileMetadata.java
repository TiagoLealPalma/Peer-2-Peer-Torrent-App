package auxiliary.Structs;

import java.io.Serializable;

public class FileMetadata implements Serializable {
    private String fileName;
    private byte[] hash;
    private int length; // Length in blocks of the file

    public FileMetadata(String fileName, byte[] hash, int length){
        this.fileName = fileName;
        this.hash = hash;
        this.length = length; // In bytes
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (obj == null || getClass() != obj.getClass()) return false;

        FileMetadata that = (FileMetadata) obj;
        return java.util.Arrays.equals(this.hash, that.getHash());
    }

    @Override
    public String toString() {
        return "FileMetadata{ fileName='" + fileName + '}';
    }

    @Override
    public int hashCode(){
        return java.util.Arrays.hashCode(hash);
    }

    public String getFileName() {
        return fileName;
    }

    public byte[] getHash() { return hash; }

    public int getLength() {return length;}
}