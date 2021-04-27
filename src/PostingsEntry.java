import java.util.Arrays;
import java.util.stream.Collectors;

public class PostingsEntry {
    private Searcher.Page page;
    private float score = 0f;
    private String ID;

    public PostingsEntry(Searcher.Page p) {
        page = p;
    }

    public String getDescription() {
        return page.title;
    }

    public String getContent() {
        return page.opening_text + "\n\nCategories: " + Arrays.stream(page.category).collect(Collectors.joining(", "));
    }

    public String getID() {
        return ID;
    }

    public void setID(String s) {
        ID = s;
    }

    public float getScore() {
        return score;
    }

    public void setScore(float s) {
        score = s;
    }
}
