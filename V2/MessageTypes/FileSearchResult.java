package V2.MessageTypes;

import V2.Structs.FileMetadata;

import java.util.List;

public class FileSearchResult implements Message{
    private final List<FileMetadata> list;
    private final boolean uiUpdate;
    public FileSearchResult(List<FileMetadata> list, boolean uiUpdate) {
        this.list = list;
        this.uiUpdate = uiUpdate;
    }

    public List<FileMetadata> getList() {
        return list;
    }

    public boolean isUIUpdate() {
        return uiUpdate;
    }
}
