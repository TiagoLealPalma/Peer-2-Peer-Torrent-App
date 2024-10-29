package V2.MessageTypes;

public class WordSearchRequest implements Message{
    private final String keyWord;
    private final boolean uiUpdate;

    public WordSearchRequest(String keyWord, boolean uiUpdate) {
        this.keyWord = keyWord;
        this.uiUpdate = uiUpdate;
    }

    public String getKeyWord() {
        return keyWord;
    }
    public boolean isUIUpdate() {
        return uiUpdate;
    }
}
