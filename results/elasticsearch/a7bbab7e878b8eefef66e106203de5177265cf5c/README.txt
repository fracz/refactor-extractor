commit a7bbab7e878b8eefef66e106203de5177265cf5c
Author: Simon Willnauer <simonw@apache.org>
Date:   Wed Jan 30 17:27:35 2013 +0100

    # Rescore Feature

    The rescore feature allows te rescore a document returned by a query based
    on a secondary algorithm. Rescoring is commonly used if a scoring algorithm
    is too costly to be executed across the entire document set but efficient enough
    to be executed on the Top-K documents scored by a faster retrieval method. Rescoring
    can help to improve precision by reordering a larger Top-K window than actually
    returned to the user. Typically is it executed on a window between 100 and 500 documents
    while the actual result window requested by the user remains the same.

    # Query Rescorer

    The `query` rescorer executes a secondary query only on the Top-K results of the actual
    user query and rescores the documents based on a linear combination of the user query's score
    and the score of the `rescore_query`. This allows to execute any exposed query as a
    `rescore_query` and supports a `query_weight` as well as a `rescore_query_weight` to weight the
    factors of the linear combination.

    # Rescore API

    The `rescore` request is defined along side the query part in the json request:

    ```json
    curl -s -XPOST 'localhost:9200/_search' -d {
      "query" : {
        "match" : {
          "field1" : {
            "query" : "the quick brown",
            "type" : "boolean",
            "operator" : "OR"
          }
        }
      },
      "rescore" : {
        "window_size" : 50,
        "query" : {
          "rescore_query" : {
            "match" : {
              "field1" : {
                "query" : "the quick brown",
                "type" : "phrase",
                "slop" : 2
              }
            }
          },
          "query_weight" : 0.7,
          "rescore_query_weight" : 1.2
        }
      }
    }
    ```

    Each `rescore` request is executed on a per-shard basis within the same roundtrip. Currently the rescore API
    has only one implementation (the `query` rescorer) which modifies the result set in-place. Future developments
    could include dedicated rescore results if needed by the implemenation ie. a pair-wise reranker.
    *Note:* Only regualr queries are rescored, if the search type is set to `scan` or `count` rescorers are not executed.

    Closes #2640