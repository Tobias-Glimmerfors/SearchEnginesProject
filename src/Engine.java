import java.util.Scanner;

public class Engine 
{
    public static void main( String[] args )
    {
        Searcher searcher = new Searcher();
        Scanner scanner = new Scanner(System.in);
        
        while (true) {
            String q = scanner.nextLine();
            System.out.println("Query=" + q);
            PostingsList res = searcher.search(q);
            System.out.println("Num. res: " + Integer.toString(res.size()));
            
            for (PostingsEntry e: res) {
                System.out.println(e.getDescription());
            }
        }
    }
}
