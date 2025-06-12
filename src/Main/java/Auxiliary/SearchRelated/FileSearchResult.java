package auxiliary.SearchRelated;

import auxiliary.Message;
import auxiliary.Structs.FileMetadata;

import java.util.List;
import java.util.Stack;

public class FileSearchResult implements Message {
    private final int port;
    private final List<FileMetadata> list;
    private Stack<Integer> path;
    public FileSearchResult(List<FileMetadata> list, Stack<Integer> path, int port)
    {
        this.list = list;
        this.path = path;
        this.port = port;
    }

    public List<FileMetadata> getList() {
        return list;
    }

    public Stack<Integer> getPath() {
        return path;
    }

    public int getPort() {
        return port;
    }

    public void pathPush(){
        path.push(port);
    }

    public int pathPop(){
        return path.pop();
    }

    public boolean isRootNode(){
        return path.isEmpty();
    }

}
