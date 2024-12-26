package V2.Auxiliary.SearchRelated;

import V2.Auxiliary.Message;

import java.util.ArrayList;
import java.util.List;
import java.util.Stack;


public class WordSearchRequest implements Message {
    private final String keyWord;
    public List<Integer> connectionsAsked;
    public Stack<Integer> path;

    public WordSearchRequest(String keyWord, List<Integer> connectionsAsked, Stack<Integer> path) {
        this.keyWord = keyWord;
        this.connectionsAsked = new ArrayList<>(connectionsAsked);
        this.path = path;
    }

    public String getKeyWord() {
        return keyWord;
    }

    public List<Integer> getConnectionsAsked() {
        return connectionsAsked;
    }

    public Stack<Integer> getPath() {
        return path;
    }
}
