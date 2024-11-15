package V2.Auxiliary.MessageTypes.DownloadRelated;

import V2.Auxiliary.MessageTypes.Message;
import V2.Auxiliary.Structs.FileMetadata;

public class FileDownloadResponse implements Message{
    private final String PROCESS_ID;

    public FileDownloadResponse(String processID) {
        this.PROCESS_ID = processID;
    }

    public String getPROCESS_ID() {return PROCESS_ID;}
}
