import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

public class PostingsList implements Iterable<PostingsEntry> {
    private List<PostingsEntry> list;

    public PostingsList() {
        list = new ArrayList<PostingsEntry>();
    }
    
    public PostingsList(List<PostingsEntry> l) {
        list = l;
    }

    public int size() {
        return list.size();
    }

    public PostingsEntry get(int i) {
        return list.get(i);
    }

    public void add(PostingsEntry e) {
        list.add(e);
    }

    @Override
    public Iterator<PostingsEntry> iterator() {
        return list.iterator();
    }
}
