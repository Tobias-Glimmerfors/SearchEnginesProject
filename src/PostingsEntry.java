
public class PostingsEntry {
    private Searcher.Page page;

    public PostingsEntry(Searcher.Page p) {
        page = p;
    }

    public String getDescription() {
        return page.title;
    }

    public String getContent() {
        return page.title;
    }
}
