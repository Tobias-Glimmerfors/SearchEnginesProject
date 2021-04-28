import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

public class PostingsEntry {
    private Searcher.Page page;
    private float score = 0f;
    private String ID;
    private List<String> highlights = new ArrayList<String>();

    public PostingsEntry(Searcher.Page p) {
        page = p;
    }

    public String getDescription() {
        return page.title;
    }

    public String getContent() {
        String endpoint = page.title.replace(" ", "_");
        try {
            endpoint = URLEncoder.encode(endpoint, "UTF-8");
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }

        String highlightedSentences = highlights.stream().map(h -> "\"" + h + "\"").collect(Collectors.joining("\n"));

        String url = "<a href='http://en.wikiquote.org/wiki/" + endpoint + "'>" + page.title + "</a>";
        String categories = Arrays.stream(page.category).collect(Collectors.joining(", "));
        String content = "\n" + url + "\n\n" + page.opening_text + "\n\nHighlights:\n" + highlightedSentences + "\n\nCategories: " + categories;

        return content.replace("\n", "<br/>");
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

    public List<String> getHighlights() {
        return highlights;
    }

    public void setHighlights(List<String> l) {
        highlights = l;
    }
}
