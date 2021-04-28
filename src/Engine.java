import org.apache.http.HttpHost;
import org.elasticsearch.action.index.IndexRequest;
import org.elasticsearch.action.search.SearchRequest;
import org.elasticsearch.client.RequestOptions;
import org.elasticsearch.client.RestClient;
import org.elasticsearch.client.RestHighLevelClient;
import org.elasticsearch.common.xcontent.XContentType;
import org.elasticsearch.index.query.MatchQueryBuilder;
import org.elasticsearch.search.SearchHits;
import org.elasticsearch.search.builder.SearchSourceBuilder;

import java.io.IOException;

import com.google.gson.Gson;

/*
 *   This file is part of the computer assignment for the
 *   Information Retrieval course at KTH.
 *
 *   Johan Boye, 2017
 */

/**
 *  This is the main class for the search engine.
 */
public class Engine {

    /** The searcher used to search the index. */
    Searcher searcher;

    /** The engine GUI. */
    SearchGUI gui;

    /** Lock to prevent simultaneous access to the index. */
    Object indexLock = new Object();

    /** The file containing the logo. */
    String pic_file = "../ir20.png";

    RestHighLevelClient client;
    Profile profile;

    final String ELASTIC_HOST = "localhost";
    final String ELASTIC_PROTOCOL = "http";
    final int ELASTIC_PORT = 9200;
    private final String PROFILE_INDEX = "profile";
    private Gson gson = new Gson();

    /* ----------------------------------------------- */
    class ShutdownHook extends Thread {
        private Profile profile;
        
        public ShutdownHook(Profile p) {
            profile = p;
        }

        public void run() {
            IndexRequest req = new IndexRequest(PROFILE_INDEX);
            req.id(profile.getUser());
            req.source(gson.toJson(profile), XContentType.JSON);
            req.type("profile");

            try {
                client.index(req, RequestOptions.DEFAULT);
            } catch (IOException e) {
                e.printStackTrace();
                System.out.println("Could not save profile...");
            } catch (Exception e) {
                e.printStackTrace();
                System.out.println("Could not save profile...");
            }
        }
    }

    /**
     *   Constructor.
     *   Indexes all chosen directories and files
     */
    public Engine( String[] args ) {
        searcher = new Searcher(this);
        gui = new SearchGUI( this );
        gui.init();
        profile = new Profile("username");
        client = new RestHighLevelClient(
            RestClient.builder(new HttpHost(ELASTIC_HOST, ELASTIC_PORT, ELASTIC_PROTOCOL))
        );

        gui.displayInfoText("Loading user profile...");

        SearchRequest req = new SearchRequest(PROFILE_INDEX);
        SearchSourceBuilder src = new SearchSourceBuilder();
        src.query(new MatchQueryBuilder("user", profile.getUser()));
        req.source(src);

        try {
            SearchHits hits = client.search(req, RequestOptions.DEFAULT).getHits();
            
            if (hits.getTotalHits() > 0) {
                profile = gson.fromJson(hits.getAt(0).getSourceAsString(), Profile.class);
                gui.displayInfoText("Profile loaded");
            } else {
                gui.displayInfoText("New profile created");
            }
            
        } catch (IOException e) {
            e.printStackTrace();
            gui.displayInfoText("New profile created");
        } catch (Exception e) {
            e.printStackTrace();
            gui.displayInfoText("New profile created");
        }

        // save profile on exit
        Runtime.getRuntime().addShutdownHook(new ShutdownHook(profile));
    }

    /* ----------------------------------------------- */


    public static void main( String[] args ) {
        new Engine( args );
    }

}
