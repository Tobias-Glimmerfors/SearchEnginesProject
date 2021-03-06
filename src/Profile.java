import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class Profile {
    private String user;
    private List<String> favors = new ArrayList<String>();
    private List<String> disfavors = new ArrayList<String>();
    private ArrayList<Query> prevQueries = new ArrayList<Query>();

    class Query {
        // TODO: use relevanceFeedback IDs instead of result IDs

        private String query;
        private List<String> IDs;

        public Query(String q, List<String> resIDs) {
            query = q;
            IDs = resIDs;
        }

        public String getQuery() {
            return query;
        }

        public int size() {
            return IDs.size();
        }

        public String get(int i) {
            return IDs.get(i);
        }

        public Stream<String> getStream() {
            return IDs.stream();
        }
    }

    public Profile(String s) {
        user = s;
    }

    public String getUser() {
        return user;
    }

    public void addFavor(String s) {
      System.out.println("adding favor: " + s);
        favors.add(s);
    }

    public void addDisfavor(String s) {
        System.out.println("adding disfavor: " + s);
        disfavors.add(s);
    }

    public String favorsString() {
        return favors.stream().collect(Collectors.joining(" "));
    }

    public String disfavorsString() {
        return disfavors.stream().collect(Collectors.joining(" "));
    }

    public Iterator<String> favorsIterator() {
        return favors.iterator();
    }

    public Iterator<String> disfavorsIterator() {
        return disfavors.iterator();
    }

    public void removeFavor(String s) {
        System.out.println("removing favor: " + s);
        favors.remove(s);
    }

    public void removeDisfavor(String s) {
        System.out.println("removing disfavor: " + s);
        disfavors.remove(s);
    }

    public int numberOfFavors() {
        return favors.size();
    }

    public int numberOfDisfavors() {
        return disfavors.size();
    }

    public void addQuery(String q) {
        addQuery(q, new PostingsList());
    }

    public void addQuery(String q, PostingsList res) {
        ArrayList<String> IDs = new ArrayList<String>();
        
        boolean addedEntry = false;

        for (Query query : prevQueries) {
            if (query.query.equals(q)) {
                for (PostingsEntry e : res) {
                    query.IDs.add(e.getID());
                }

                addedEntry = true;
                break;
            }
        }

        if (!addedEntry) {
            for (PostingsEntry e : res) {
                IDs.add(e.getID());
            }

            prevQueries.add(new Query(q, IDs));
        }
    }

    public void addClickedEntry(String q, PostingsEntry e) {
        boolean addedEntry = false;

        for (Query query : prevQueries) {
            if (query.query.equals(q)) {
                query.IDs.add(e.getID());
                addedEntry = true;
                break;
            }
        }

        if (!addedEntry) {
            prevQueries.add(new Query(q, new ArrayList<String>(Arrays.asList(e.getID()))));
        }
    }

    public int prevQueriesSize() {
        return prevQueries.size();
    }

    public Query getPrevQuery(int i) {
        return prevQueries.get(i);
    }

    public Stream<Query> getPrevQueryStream() {
        return prevQueries.stream();
    }
}
