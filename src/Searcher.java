import java.io.IOException;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

import com.alibaba.fastjson.JSON;

import org.apache.http.HttpHost;
import org.elasticsearch.action.search.SearchRequest;
import org.elasticsearch.action.search.SearchResponse;
import org.elasticsearch.client.RequestOptions;
import org.elasticsearch.client.RestClient;
import org.elasticsearch.client.RestHighLevelClient;
import org.elasticsearch.index.query.QueryBuilders;
import org.elasticsearch.search.SearchHit;
import org.elasticsearch.search.SearchHits;
import org.elasticsearch.search.builder.SearchSourceBuilder;

public class Searcher {
    private RestHighLevelClient client;

    class Page {
        String title;
    }

    public Searcher() {
        client = new RestHighLevelClient(
            RestClient.builder(new HttpHost("localhost:9200"))
        );        
    }

    public List<Page> searchPage(String query) throws IOException {
        SearchRequest searchRequest = new SearchRequest("enwikiquotes");
        
        SearchSourceBuilder sourceBuilder = new SearchSourceBuilder();
        sourceBuilder.query(QueryBuilders.simpleQueryStringQuery(query));
        
        searchRequest.source(sourceBuilder);

        String[] includeFields = new String[] {"title"};

        sourceBuilder.fetchSource(includeFields, null);

        SearchResponse searchResponse = client.search(searchRequest, RequestOptions.DEFAULT);
        SearchHit[] hits = searchResponse.getHits().getHits();

        List<Page> results = Arrays.stream(hits)
            .map(hit -> JSON.parseObject(hit.getSourceAsString(), Page.class))
            .collect(Collectors.toList());

        return results;
    }

}

