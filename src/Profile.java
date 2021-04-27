import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;

public class Profile {
    private String user;
    private List<String> favors = new ArrayList<String>();
    private List<String> disfavors = new ArrayList<String>();
    private HashMap<String, List<String>> prevQueries = new HashMap<String, List<String>>();

    public Profile(String s) {
        user = s;
    }

    public void addFavor(String s) {
        favors.add(s);
    }

    public void addDisfavor(String s) {
        disfavors.add(s);
    }

    public Iterator<String> favorsIterator() {
        return favors.iterator();
    }

    public Iterator<String> disfavorsIterator() {
        return disfavors.iterator();
    }

    public void addQuery(String q, PostingsList res) {
        ArrayList<String> IDs = new ArrayList<String>();
        for (PostingsEntry e : res) {
            IDs.add(e.getID());
        }

        prevQueries.put(q, IDs);
    }
}
