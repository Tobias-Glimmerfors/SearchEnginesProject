#!/bin/sh
export es=localhost:9200
export site=en.wikipedia.org
export index=enwikiquote
curl -XDELETE $es/$index?pretty
curl -H 'Content-Type: application/json' -s 'https://'$site'/w/api.php?action=cirrus-settings-dump&format=json&formatversion=2' |
  jq '{
    settings: { 
        index: { 
            analysis: .content.page.index.analysis, 
            similarity: .content.page.index.similarity 
        } 
    }
  }' |
curl -H 'Content-Type: application/json' -XPUT $es/$index?pretty -d @-
curl -H 'Content-Type: application/json' -s 'https://'$site'/w/api.php?action=cirrus-mapping-dump&format=json&formatversion=2' |
  jq .content |
  sed 's/"index_analyzer"/"analyzer"/' |
  sed 's/"position_offset_gap"/"position_increment_gap"/' |
  curl -H 'Content-Type: application/json' -XPUT $es/$index/_mapping/page?pretty -d @-
