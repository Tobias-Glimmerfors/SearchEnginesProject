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
import org.elasticsearch.index.query.MoreLikeThisQueryBuilder;
import org.elasticsearch.index.query.QueryBuilder;
import org.elasticsearch.search.SearchHits;
import org.elasticsearch.search.builder.SearchSourceBuilder;
import org.elasticsearch.search.fetch.subphase.highlight.HighlightBuilder;

public class Searcher {
    private RestHighLevelClient client;
    private Gson gson;
    private Engine engine;
    private final int RESULT_SIZE = 50;

    // which elasticsearch index to search in
    private final String SEARCH_INDEX = "enwikiquote";

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
            RestClient.builder(new HttpHost(engine.ELASTIC_HOST, engine.ELASTIC_PORT, engine.ELASTIC_PROTOCOL))
        );
    }

    QueryBuilder getProperQuery(String query) {
        // "basic" query
        SimpleQueryStringBuilder queryBuilder = new SimpleQueryStringBuilder(query);
        queryBuilder.field("title", 1f); // add a field to query with weight
        queryBuilder.field("text", 1f);
        queryBuilder.field("category", 1f);

        // combine query with favored categories
        BoolQueryBuilder boolQueryBuilder = new BoolQueryBuilder();
        boolQueryBuilder.must(queryBuilder);
        boolQueryBuilder.should(new MatchQueryBuilder("category", engine.profile.favorsString()));

        // add history where the sum of boosts is 1
        float a_inverse = 0f;

        for (int i = 0; i < engine.profile.prevQueriesSize(); i++) {
            a_inverse += 1f / ((float) (i + 2));
        }

        a_inverse = 0.9f * a_inverse; // equal weighting for relevance/clicked and history

        for (int i = 0; i < engine.profile.prevQueriesSize(); i++) {
            SimpleQueryStringBuilder historyQueryBuilder = new SimpleQueryStringBuilder(engine.profile.getPrevQuery(i).getQuery());
            historyQueryBuilder.field("title", 1f);
            historyQueryBuilder.field("text", 1f);
            historyQueryBuilder.field("category", 1f);

            historyQueryBuilder.boost((1f / a_inverse) / ((float) (engine.profile.prevQueriesSize() - i + 1)));
            boolQueryBuilder.should(historyQueryBuilder);
        }

        // if there are clicked/relevant docs
        if (engine.profile.getPrevQueryStream().map(q -> q.size()).mapToInt(Integer::intValue).sum() > 0) {
            // add relevant and clicked documents
            MoreLikeThisQueryBuilder mltQueryBuilder = new MoreLikeThisQueryBuilder(
                new String[] {"title", "text", "category"}, // fields to compare
                null, 
                engine.profile.getPrevQueryStream() // convert every clicked and relevant ID to an array of MLT items
                .flatMap(q -> q.getStream())
                .map(s -> new MoreLikeThisQueryBuilder.Item(SEARCH_INDEX, "page", s))
                .collect(Collectors.toList())
                .toArray(new MoreLikeThisQueryBuilder.Item[0])
            );
            mltQueryBuilder.include(true);
            mltQueryBuilder.boost(0.1f); // equal weighting for history and relevance/clicked
            boolQueryBuilder.should(mltQueryBuilder);
        }

        // boost the searched and preferred queries while demote disfavored categories
        BoostingQueryBuilder favorQuery = new BoostingQueryBuilder(
            boolQueryBuilder,
            new MatchQueryBuilder("category", engine.profile.disfavorsString())
        );
        favorQuery.negativeBoost(0.5f);

        return favorQuery;        
    }

    BoolQueryBuilder getMLTQuery(QueryBuilder q, PostingsList list) {
        BoolQueryBuilder query = new BoolQueryBuilder();
        MoreLikeThisQueryBuilder mltQuery = new MoreLikeThisQueryBuilder(
            new String[] {"title", "text", "category"}, // fields to compare
            null, // custom texts to compare with fields
            list.stream() // document IDs to compare with fields
                .map(entry -> new MoreLikeThisQueryBuilder.Item(SEARCH_INDEX, "page", entry.getID()))
                .toArray(MoreLikeThisQueryBuilder.Item[]::new)
        );

        mltQuery.include(true); // include input docs in result

        query.must(q);
        query.should(mltQuery);
        
        return query;
    }

    SearchRequest getRequest(QueryBuilder query, int from) {
        String[] includeFields = new String[] {"title", "category", "opening_text", "popularity_score"};

        SearchRequest searchRequest = new SearchRequest(SEARCH_INDEX); // create the request object
        SearchSourceBuilder sourceBuilder = new SearchSourceBuilder(); // something to do with sourcing?
        sourceBuilder.query(query); // add the query to the source object
        sourceBuilder.fetchSource(includeFields, null); // set with fields to include and exclude
        sourceBuilder.from(from);
        sourceBuilder.size(RESULT_SIZE);
        
        // HighlightBuilder highlighter = new HighlightBuilder();
        // highlighter.field("text");
        // highlighter.preTags("<strong>");
        // highlighter.postTags("</strong>");

        // sourceBuilder.highlighter(highlighter);

        searchRequest.source(sourceBuilder); // add the source to the request
        return searchRequest;
    }

    PostingsList getResults(SearchRequest searchRequest) {
      PostingsList res;

      try {
          SearchResponse searchResponse = client.search(searchRequest, RequestOptions.DEFAULT); // perform the search
          SearchHits hits = searchResponse.getHits(); // fetch results

          List<PostingsEntry> results = Arrays.stream(hits.getHits()) // transform results into a list of Pages
              .map(hit -> {
                  PostingsEntry e = new PostingsEntry(gson.fromJson(hit.getSourceAsString(), Page.class));
                  e.setScore(hit.getScore());
                  e.setID(hit.getId());
                //   List<String> highlights = hit.getHighlightFields()
                //     .values()
                //     .stream()
                //     .flatMap(field -> Arrays.asList(field.fragments())
                //     .stream()
                //     .map(fragment -> fragment.string()))
                //     .collect(Collectors.toList());
                //   e.setHighlights(highlights);

                  return e;
              })
              .collect(Collectors.toList());

          res = new PostingsList(results, hits.getTotalHits());
      } catch (IOException e) {
          e.printStackTrace();
          res = new PostingsList();
      } catch (Exception e) {
          e.printStackTrace();
          res = new PostingsList();
      }
      return res;
    }

    public PostingsList search(String q) {
        QueryBuilder query = getProperQuery(q);
        SearchRequest searchRequest = getRequest(query, 0);
        PostingsList res = getResults(searchRequest);
        res.setQuery(query);
        engine.profile.addQuery(q);

        return res;
    }

    public PostingsList relevanceSearch(String q, PostingsList relevantDocs) {
        QueryBuilder query = getMLTQuery(getProperQuery(q), relevantDocs);
        SearchRequest searchRequest = getRequest(query, 0);
        PostingsList res = getResults(searchRequest);
        res.setQuery(query);
        engine.profile.addQuery(q, relevantDocs);
        return res;
    }

    public PostingsList getMoreResults(PostingsList res) {
        SearchRequest searchRequest = getRequest(res.getQuery(), res.size());
        res.add(getResults(searchRequest));
        return res;
    }

}
