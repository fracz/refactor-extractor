commit 2e68207d6ddfff5059a0ca8d1bdefc96f780e576
Author: Martijn van Groningen <martijn.v.groningen@gmail.com>
Date:   Mon Jan 28 15:18:18 2013 +0100

    Updated suggest api.

    # Suggest feature
    The suggest feature suggests similar looking terms based on a provided text by using a suggester. At the moment there the only supported suggester is `fuzzy`. The suggest feature is available from version `0.21.0`.

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

    Several suggestions can be specified per request. Each suggestion is identified with an arbitary name. In the example below two suggestions are requested. Both `my-suggest-1` and `my-suggest-2` suggestions use the `fuzzy` suggester, but have a different `text`.

    ```
    "suggest" : {
      "my-suggest-1" : {
        "text" : "the amsterdma meetpu",
        "fuzzy" : {
          "field" : "body"
        }
      },
      "my-suggest-2" : {
        "text" : "the rottredam meetpu",
        "fuzzy" : {
          "field" : "title",
        }
      }
    }
    ```

    The below suggest response example includes the suggestion response for `my-suggest-1` and `my-suggest-2`. Each suggestion part contains entries. Each entry is effectively a token from the suggest text and contains the suggestion entry text, the original start offset and length in the suggest text and if found an arbitary number of options.

    ```
    {
      ...
      "suggest": {
        "my-suggest-1": [
          {
            "text" : "amsterdma",
            "offset": 4,
            "length": 9,
            "options": [
               ...
            ]
          },
          ...
        ],
        "my-suggest-2" : [
          ...
        ]
      }
      ...
    }
    ```

    Each options array contains a option object that includes the suggested text, its document frequency and score compared to the suggest entry text. The meaning of the score depends on the used suggester. The fuzzy suggester's score is based on the edit distance.

    ```
    "options": [
      {
        "text": "amsterdam",
        "freq": 77,
        "score": 0.8888889
      },
      ...
    ]
    ```

    # Global suggest text

    To avoid repitition of the suggest text, it is possible to define a global text. In the example below the suggest text is defined globally and applies to the `my-suggest-1` and `my-suggest-2` suggestions.

    ```
    "suggest" : {
      "text" : "the amsterdma meetpu"
      "my-suggest-1" : {
        "fuzzy" : {
          "field" : "title"
        }
      },
      "my-suggest-2" : {
        "fuzzy" : {
          "field" : "body"
        }
      }
    }
    ```

    The suggest text can in the above example also be specied as suggestion specific option. The suggest text specified on suggestion level override the suggest text on the global level.

    # Other suggest example.

    In the below example we request suggestions for the following suggest text: `devloping distibutd saerch engies` on the `title` field with a maximum of 3 suggestions per term inside the suggest text. Note that in this example we use the `count` search type. This isn't required, but a nice optimalization. The suggestions are gather in the `query` phase and in the case that we only care about suggestions (so no hits) we don't need to execute the `fetch` phase.

    ```
    curl -s -XPOST 'localhost:9200/_search?search_type=count' -d '{
      "suggest" : {
        "my-title-suggestions-1" : {
          "text" : "devloping distibutd saerch engies",
          "fuzzy" : {
            "size" : 3,
            "field" : "title"
          }
        }
      }
    }'
    ```

    The above request could yield the response as stated in the code example below. As you can see if we take the first suggested options of each suggestion entry we get `developing distributed search engines` as result.

    ```
    {
      ...
      "suggest": {
        "my-title-suggestions-1": [
          {
            "text": "devloping",
            "offset": 0,
            "length": 9,
            "options": [
              {
                "text": "developing",
                "freq": 77,
                "score": 0.8888889
              },
              {
                "text": "deloping",
                "freq": 1,
                "score": 0.875
              },
              {
                "text": "deploying",
                "freq": 2,
                "score": 0.7777778
              }
            ]
          },
          {
            "text": "distibutd",
            "offset": 10,
            "length": 9,
            "options": [
              {
                "text": "distributed",
                "freq": 217,
                "score": 0.7777778
              },
              {
                "text": "disributed",
                "freq": 1,
                "score": 0.7777778
              },
              {
                "text": "distribute",
                "freq": 1,
                "score": 0.7777778
              }
            ]
          },
          {
            "text": "saerch",
            "offset": 20,
            "length": 6,
            "options": [
              {
                "text": "search",
                "freq": 1038,
                "score": 0.8333333
              },
              {
                "text": "smerch",
                "freq": 3,
                "score": 0.8333333
              },
              {
                "text": "serch",
                "freq": 2,
                "score": 0.8
              }
            ]
          },
          {
            "text": "engies",
            "offset": 27,
            "length": 6,
            "options": [
              {
                "text": "engines",
                "freq": 568,
                "score": 0.8333333
              },
              {
                "text": "engles",
                "freq": 3,
                "score": 0.8333333
              },
              {
                "text": "eggies",
                "freq": 1,
                "score": 0.8333333
              }
            ]
          }
        ]
      }
      ...
    }
    ```

    # Common suggest options:
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