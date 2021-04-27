import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.stream.Collectors;

public class Profile {
    private String user;
    private List<String> favors = new ArrayList<String>();
    private List<String> disfavors = new ArrayList<String>();
    private HashMap<String, List<String>> prevQueries = new HashMap<String, List<String>>();

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

    public void addQuery(String q, PostingsList res) {
        ArrayList<String> IDs = new ArrayList<String>();
        for (PostingsEntry e : res) {
            IDs.add(e.getID());
        }

        prevQueries.put(q, IDs);
    }
}
