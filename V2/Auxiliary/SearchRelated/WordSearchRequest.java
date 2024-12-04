package V2.Auxiliary.SearchRelated;

import V2.Auxiliary.Message;

public class WordSearchRequest implements Message {
    private final String keyWord;

    public WordSearchRequest(String keyWord) {
        this.keyWord = keyWord;
    }

    public String getKeyWord() {
        return keyWord;
    }
}
