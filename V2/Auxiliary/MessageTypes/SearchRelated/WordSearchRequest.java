package V2.Auxiliary.MessageTypes.SearchRelated;

import V2.Auxiliary.MessageTypes.Message;

public class WordSearchRequest implements Message {
    private final String keyWord;

    public WordSearchRequest(String keyWord) {
        this.keyWord = keyWord;
    }

    public String getKeyWord() {
        return keyWord;
    }
}
