package V2.Auxiliary.MessageTypes.DownloadRelated;

import V2.Auxiliary.MessageTypes.Message;
import V2.Auxiliary.Structs.FileMetadata;

import java.util.UUID;

public class FileDownloadRequest implements Message {
    private FileMetadata file;
    private String id = UUID.randomUUID().toString();

    public FileDownloadRequest(FileMetadata file) {
        this.file = file;
    }
    public FileMetadata getFile() {return file;}
    public String getId() {return id;}
}
