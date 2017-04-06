commit 98a674fc6ed8516f8a20641950e0c149b0494bee
Author: Martijn van Groningen <martijn.v.groningen@gmail.com>
Date:   Thu Jan 24 15:38:35 2013 +0100

    Added suggest api.

    # Suggest feature
    The suggest feature suggests similar looking terms based on a provided text by using a suggester. At the moment there the only supported suggester is `fuzzy`. The suggest feature is available since version `0.21.0`.

    # Fuzzy suggester
    The `fuzzy` suggester suggests terms based on edit distance. The provided suggest text is analyzed before terms are suggested. The suggested terms are provided per analyzed suggest text token. The `fuzzy` suggester doesn't take the query into account that is part of request.

    # Suggest API
    The suggest request part is defined along side the query part as top field in the json request.

    ```
    curl -s -XPOST 'localhost:9200/_search' -d '{
        "query" : {
            ...
        },
        "suggest" : {
            ...
        }
    }'
    ```

    Several suggestions can be specified per request. Each suggestion is identified with an arbitary name. In the example below two suggestions are requested. The `my-suggest-1` suggestion uses the `body` field and `my-suggest-2` uses the `title` field. The `type` field is a required field and defines what suggester to use for a suggestion.

    ```
    "suggest" : {
        "suggestions" : {
            "my-suggest-1" : {
                "type" : "fuzzy",
                "field" : "body",
                "text" : "the amsterdma meetpu"
            },
            "my-suggest-2" : {
                "type" : "fuzzy",
                "field" : "title",
                "text" : "the rottredam meetpu"
            }
        }
    }
    ```

    The below suggest response example includes the suggestions part for `my-suggest-1` and `my-suggest-2`. Each suggestion part contains a terms array, that contains all terms outputted by the analyzed suggest text. Each term object includes the term itself, the original start and end offset in the suggest text and if found an arbitary number of suggestions.

    ```
    {
        ...
        "suggest": {
            "my-suggest-1": {
                "terms" : [
                  {
                    "term" : "amsterdma",
                    "start_offset": 5,
                    "end_offset": 14,
                    "suggestions": [
                       ...
                    ]
                  }
                  ...
                ]
            },
            "my-suggest-2" : {
              "terms" : [
                ...
              ]
            }
        }
    ```

    Each suggestions array contains a suggestion object that includes the suggested term, its document frequency and score compared to the suggest text term. The meaning of the score depends on the used suggester. The fuzzy suggester's score is based on the edit distance.

    ```
    "suggestions": [
        {
            "term": "amsterdam",
            "frequency": 77,
            "score": 0.8888889
        },
        ...
    ]
    ```

    # Global suggest text

    To avoid repitition of the suggest text, it is possible to define a global text. In the example below the suggest text is a global option and applies to the `my-suggest-1` and `my-suggest-2` suggestions.

    ```
    "suggest" : {
        "suggestions" : {
            "text" : "the amsterdma meetpu",
            "my-suggest-1" : {
                "type" : "fuzzy",
                "field" : "title"
            },
            "my-suggest-2" : {
                "type" : "fuzzy",
                "field" : "body"
            }
        }
    }
    ```

    The suggest text can be specied as global option or as suggestion specific option. The suggest text specified on suggestion level override the suggest text on the global level.

    # Other suggest example.

    In the below example we request suggestions for the following suggest text: `devloping distibutd saerch engies` on the `title` field with a maximum of 3 suggestions per term inside the suggest text. Note that in this example we use the `count` search type. This isn't required, but a nice optimalization. The suggestions are gather in the `query` phase and in the case that we only care about suggestions (so no hits) we don't need to execute the `fetch` phase.

    ```
    curl -s -XPOST 'localhost:9200/_search?search_type=count' -d '{
      "suggest" : {
          "suggestions" : {
            "my-title-suggestions" : {
              "suggester" : "fuzzy",
              "field" : "title",
              "text" : "devloping distibutd saerch engies",
              "size" : 3
            }
          }
      }
    }'
    ```

    The above request could yield the response as stated in the code example below. As you can see if we take the first suggested term of each suggest text term we get `developing distributed search engines` as result.

    ```
    {
      ...
      "suggest": {
        "my-title-suggestions": {
          "terms": [
            {
              "term": "devloping",
              "start_offset": 0,
              "end_offset": 9,
              "suggestions": [
                {
                  "term": "developing",
                  "frequency": 77,
                  "score": 0.8888889
                },
                {
                  "term": "deloping",
                  "frequency": 1,
                  "score": 0.875
                },
                {
                  "term": "deploying",
                  "frequency": 2,
                  "score": 0.7777778
                }
              ]
            },
            {
              "term": "distibutd",
              "start_offset": 10,
              "end_offset": 19,
              "suggestions": [
                {
                  "term": "distributed",
                  "frequency": 217,
                  "score": 0.7777778
                },
                {
                  "term": "disributed",
                  "frequency": 1,
                  "score": 0.7777778
                },
                {
                  "term": "distribute",
                  "frequency": 1,
                  "score": 0.7777778
                }
              ]
            },
            {
              "term": "saerch",
              "start_offset": 20,
              "end_offset": 26,
              "suggestions": [
                {
                  "term": "search",
                  "frequency": 1038,
                  "score": 0.8333333
                },
                {
                  "term": "smerch",
                  "frequency": 3,
                  "score": 0.8333333
                },
                {
                  "term": "serch",
                  "frequency": 2,
                  "score": 0.8
                }
              ]
            },
            {
              "term": "engies",
              "start_offset": 27,
              "end_offset": 33,
              "suggestions": [
                {
                  "term": "engines",
                  "frequency": 568,
                  "score": 0.8333333
                },
                {
                  "term": "engles",
                  "frequency": 3,
                  "score": 0.8333333
                },
                {
                  "term": "eggies",
                  "frequency": 1,
                  "score": 0.8333333
                }
              ]
            }
          ]
        }
      }
      ...
    }
    ```

    # Common suggest options:
    * `suggester` - The suggester implementation type. The only supported value is 'fuzzy'. This is a required option.
    * `text` - The suggest text. The suggest text is a required option that needs to be set globally or per suggestion.

    # Common fuzzy suggest options
    * `field` - The field to fetch the candidate suggestions from. This is an required option that either needs to be set globally or per suggestion.
    * `analyzer` - The analyzer to analyse the suggest text with. Defaults to the search analyzer of the suggest field.
    * `size` - The maximum corrections to be returned per suggest text token.
    * `sort` - Defines how suggestions should be sorted per suggest text term. Two possible value:
    ** `score` - Sort by sore first, then document frequency and then the term itself.
    ** `frequency` - Sort by document frequency first, then simlarity score and then the term itself.
    * `suggest_mode` - The suggest mode controls what suggestions are included or controls for what suggest text terms, suggestions should be suggested. Three possible values can be specified:
    ** `missing` - Only suggest terms in the suggest text that aren't in the index. This is the default.
    ** `popular` - Only suggest suggestions that occur in more docs then the original suggest text term.
    ** `always` - Suggest any matching suggestions based on terms in the suggest text.

    # Other fuzzy suggest options:
    * `lowercase_terms` - Lower cases the suggest text terms after text analyzation.
    * `max_edits` - The maximum edit distance candidate suggestions can have in order to be considered as a suggestion. Can only be a value between 1 and 2. Any other value result in an bad request error being thrown. Defaults to 2.
    * `min_prefix` - The number of minimal prefix characters that must match in order be a candidate suggestions. Defaults to 1. Increasing this number improves spellcheck performance. Usually misspellings don't occur in the beginning of terms.
    * `min_query_length` -  The minimum length a suggest text term must have in order to be included. Defaults to 4.
    * `shard_size` - Sets the maximum number of suggestions to be retrieved from each individual shard. During the reduce phase only the top N suggestions are returned based on the `size` option. Defaults to the `size` option. Setting this to a value higher than the `size` can be useful in order to get a more accurate document frequency for spelling corrections at the cost of performance. Due to the fact that terms are partitioned amongst shards, the shard level document frequencies of spelling corrections may not be precise. Increasing this will make these document frequencies more precise.
    * `max_inspections` - A factor that is used to multiply with the `shards_size` in order to inspect more candidate spell corrections on the shard level. Can improve accuracy at the cost of performance. Defaults to 5.
    * `threshold_frequency` - The minimal threshold in number of documents a suggestion should appear in. This can be specified as an absolute number or as a relative percentage of number of documents. This can improve quality by only suggesting high frequency terms. Defaults to 0f and is not enabled. If a value higher than 1 is specified then the number cannot be fractional. The shard level document frequencies are used for this option.
    * `max_query_frequency` - The maximum threshold in number of documents a sugges text token can exist in order to be included. Can be a relative percentage number (e.g 0.4) or an absolute number to represent document frequencies. If an value higher than 1 is specified then fractional can not be specified. Defaults to 0.01f. This can be used to exclude high frequency terms from being spellchecked. High frequency terms are usually spelled correctly on top of this this also improves the spellcheck performance.  The shard level document frequencies are used for this option.

     Closes #2585