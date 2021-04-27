import java.io.IOException;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;
import com.google.gson.Gson;
import org.apache.http.HttpHost;
import org.elasticsearch.action.search.SearchRequest;
import org.elasticsearch.action.search.SearchResponse;
import org.elasticsearch.client.RequestOptions;
import org.elasticsearch.client.RestClient;
import org.elasticsearch.client.RestHighLevelClient;
import org.elasticsearch.index.query.SimpleQueryStringBuilder;
import org.elasticsearch.index.query.BoolQueryBuilder;
import org.elasticsearch.index.query.BoostingQueryBuilder;
import org.elasticsearch.index.query.MatchQueryBuilder;
import org.elasticsearch.search.SearchHits;
import org.elasticsearch.search.builder.SearchSourceBuilder;

public class Searcher {
    private RestHighLevelClient client;
    private Gson gson;
    private Engine engine;

    class Page {
        public String title;
        public String[] category;
        public String opening_text;
        public float popularity_score;
    }

    public Searcher(Engine e) {
        engine = e;
        gson = new Gson();
        client = new RestHighLevelClient(
            RestClient.builder(new HttpHost("localhost", 9200, "http"))
        );        
    }

    public PostingsList search(String query) {
        SearchRequest searchRequest = new SearchRequest("enwikiquote"); // create the request object
        
        // "basic" query
        SimpleQueryStringBuilder queryBuilder = new SimpleQueryStringBuilder(query);
        queryBuilder.field("title", 1f); // add a field to query with weight
        queryBuilder.field("text", 1f); // add a field to query with weight

        // combine query with favored categories
        BoolQueryBuilder boolQueryBuilder = new BoolQueryBuilder();
        boolQueryBuilder.must(queryBuilder);
        boolQueryBuilder.should(new MatchQueryBuilder("category", engine.profile.favorsString()));

        // boost the searched and preferred queries while demote disfavored categories
        BoostingQueryBuilder favorQuery = new BoostingQueryBuilder(
            boolQueryBuilder,
            new MatchQueryBuilder("category", engine.profile.disfavorsString())
        );
        favorQuery.negativeBoost(0.5f);

        String[] includeFields = new String[] {"title", "category", "opening_text", "popularity_score"};
        
        SearchSourceBuilder sourceBuilder = new SearchSourceBuilder(); // something to do with sourcing?
        sourceBuilder.query(favorQuery); // add the query to the source object
        sourceBuilder.fetchSource(includeFields, null); // set with fields to include and exclude
        
        searchRequest.source(sourceBuilder); // add the source to the request

        PostingsList res;

        try {
            SearchResponse searchResponse = client.search(searchRequest, RequestOptions.DEFAULT); // perform the search
            SearchHits hits = searchResponse.getHits(); // fetch results

            List<PostingsEntry> results = Arrays.stream(hits.getHits()) // transform results into a list of Pages
                .map(hit -> {
                    PostingsEntry e = new PostingsEntry(gson.fromJson(hit.getSourceAsString(), Page.class));
                    e.setScore(hit.getScore());
                    e.setID(hit.getId());

                    return e;
                })
                .collect(Collectors.toList());

            res = new PostingsList(results);
        } catch (IOException e) {
            e.printStackTrace();
            res = new PostingsList();
        } catch (Exception e) {
            e.printStackTrace();
            res = new PostingsList();
        }

        engine.profile.addQuery(query, res);

        return res;
    }

}

