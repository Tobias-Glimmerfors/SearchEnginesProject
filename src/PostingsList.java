import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.stream.Stream;

import org.elasticsearch.index.query.QueryBuilder;

public class PostingsList implements Iterable<PostingsEntry> {
    private List<PostingsEntry> list;
    
    // We dont want to return all docs in the index for a given search
    // We retrieve 50 docs and fetch 50 more if needed
    // I.e., sizeInIndex is the actual result size
    private long sizeInIndex = 0; 
    private QueryBuilder query; // used if we need to fetch more results

    public PostingsList() {
        list = new ArrayList<PostingsEntry>();
    }
    
    public PostingsList(List<PostingsEntry> l) {
        list = l;
    }

    public PostingsList(List<PostingsEntry> l, long resultSize) {
        list = l;
        sizeInIndex = resultSize;
    }

    public int size() {
        return list.size();
    }

    public long numResults() {
        return sizeInIndex;
    }

    public QueryBuilder getQuery() {
        return query;
    }

    public void setQuery(QueryBuilder q) {
        query = q;
    }

    public PostingsEntry get(int i) {
        return list.get(i);
    }

    public void add(PostingsEntry e) {
        list.add(e);
    }

    public void add(PostingsList l) {
        for (PostingsEntry entry : l) {
            add(entry);
        }
    }

    public Stream<PostingsEntry> stream() {
        return list.stream();
    }

    @Override
    public Iterator<PostingsEntry> iterator() {
        return list.iterator();
    }
}
