package V2.Auxiliary.SearchRelated;

import V2.Auxiliary.Message;
import V2.Auxiliary.Structs.FileMetadata;

import java.util.List;

public class FileSearchResult implements Message {
    private final List<FileMetadata> list;
    private final WordSearchRequest request;
    public FileSearchResult(List<FileMetadata> list, WordSearchRequest request)
    {
        this.list = list;
        this.request = request;
    }

    public List<FileMetadata> getList() {
        return list;
    }

}
