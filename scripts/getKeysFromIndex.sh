#!/bin/bash
curl -g -XPOST -H 'Content-Type: application/json' http://h5:9200/sigir10_v1.0_pmnn/_search -d '{
  "query": {
    "match_all": {}
  },
  "size":0,
  "_source":false,
  "aggs": {
    "docids": {
      "terms": {"field":"pubmedId.keyword","size":30000000}
    }
  }
}' | grep -o '"key":"[^"]*"' | sed 's/"key":"//;s/"//' > keys.txt

