package V2.Auxiliary.DownloadRelated;

import V2.Auxiliary.Message;

public class FileDownloadResponse implements Message{
    private final String PROCESS_ID;

    public FileDownloadResponse(String processID) {
        this.PROCESS_ID = processID;
    }

    public String getPROCESS_ID() {return PROCESS_ID;}
}
