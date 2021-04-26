Examples
===
```
POST /enwikiquote/_search
{
  "query": {
    "more_like_this": {
      "fields": ["title", "text"],
      "like": [
        {"_index": "enwikiquote", "_id": "85102"}
      ],
      "min_term_freq": 1,
      "max_query_terms": 12
    }
  },
  "_source": ["title"]
}

POST /enwikiquote/_search
{
  "query": {
    "simple_query_string": {
      "query": "hi Peter~5",
      "default_operator": "and",
      "fields": ["title", "text"]
    }
  }
}
```

Installation steps
====

Navigate into setup/

`cd setup`

Build docker image and name it "elasticwiki"

`docker build -t elasticwiki .`

Install jq (JSON processor)

`brew install jq` or `apt-get install jq`

Download a wikipedia dump from https://dumps.wikimedia.org/other/cirrussearch/ and place the file in the setup/ directory.

Update `chunkify.sh`
- Set the `dump` variable to your wikipedia filename
- Set the `index` variable, e.g., enwikiquote or enwiki

Update `build_index.sh`
- Set the `index` variable to the same as in `chunkify.sh` in the step above

Run the docker images. This launches **elasticsearch** on localhost:9200 and **kibana** on localhost:5601.
- `docker-compose up` (with output)
- `docker-compose up -d` (detached)

Build the index

`build_index.sh`

Upload the wiki-dump to elasticsearch

`chunkify.sh`

**Note:** Elasticsearch is configured to store data persistently in `{root}/elasticdata`, i.e., the docker containers can be closed and restarted without loss of data.

Testing
====
Navigate to http://localhost:9200 to test elasticsearch. Navigate to http://localhost:5601 to test kibana.
