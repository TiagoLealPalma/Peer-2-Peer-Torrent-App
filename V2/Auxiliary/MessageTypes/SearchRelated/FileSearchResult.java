package V2.Auxiliary.MessageTypes.SearchRelated;

import V2.Auxiliary.MessageTypes.Message;
import V2.Auxiliary.Structs.FileMetadata;

import java.util.List;

public class FileSearchResult implements Message {
    private final List<FileMetadata> list;
    public FileSearchResult(List<FileMetadata> list) {
        this.list = list;
    }

    public List<FileMetadata> getList() {
        return list;
    }

}
