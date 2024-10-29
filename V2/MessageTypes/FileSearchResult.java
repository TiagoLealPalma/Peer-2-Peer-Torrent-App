package V2.MessageTypes;

import java.util.List;

public class FileSearchResult implements Message{
    private final List<String> titles;
    private final boolean uiUpdate;
    public FileSearchResult(List<String> titles, boolean uiUpdate) {
        this.titles = titles;
        this.uiUpdate = uiUpdate;
    }

    public List<String> getTitles() {
        return titles;
    }

    public boolean isUIUpdate() {
        return uiUpdate;
    }
}
