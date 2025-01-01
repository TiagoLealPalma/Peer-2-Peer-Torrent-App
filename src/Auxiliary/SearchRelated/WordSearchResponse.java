package src.Auxiliary.SearchRelated;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

public class WordSearchResponse implements Serializable {
    List<FileSearchResult> responses;


    public WordSearchResponse() {
        responses = new ArrayList<FileSearchResult>();
    }

    public WordSearchResponse(List<FileSearchResult> responses) {
        this.responses = responses;
    }

    public void addResponse(FileSearchResult response) {
        responses.add(response);
    }

    public List<FileSearchResult> getResponses() {
        return responses;
    }
}
