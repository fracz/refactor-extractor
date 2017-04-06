commit 374bbbfa7b425b7853f5843f5cd281b063655188
Author: Simon Willnauer <simonw@apache.org>
Date:   Mon Apr 8 18:12:17 2013 +0200

    # FieldData Filter

    FieldData is an in-memory representation of the term dictionary in an uninverted form. Under certain circumstances this FieldData representation can grow very large on high-cardinality fields like tokenized full-text. Depending on the use-case filtering the terms that are hold in the FieldData representation can heavily improve execution performance and application stability.
    FieldData Filters can be applied on a per-segment basis. During FieldData loading the terms enumeration is passed through a filter predicate that  either accepts or rejects a term.

    ## Frequency Filter

    The Frequency Filter acts as a high / low pass filter based on the document frequencies of a certain term within the segment that is loaded into field data. It allows to reject terms that are very high or low frequent based on absolute frequencies or percentages relative to the number of documents in the segment or more precise the number of document that have at least one value in the field that is loaded in the current segment.

    Here is an example mapping

    Here is an example mapping:

    ```json
    {
        "tweet" : {
            "properties" : {
                "locale" : {
                    "type" : "string",
                    "fielddata" : "format=paged_bytes;filter.frequency.min=0.001;filter.frequency.max=0.1",
                    "index" : "analyzed",
                }
            }
        }
    }
    ```
    ### Paramters

     * `filter.frequency.min` - the minimum document frequency (inclusive) in order to be loaded in to memory. Either a percentage if < `1.0` or an absolute value. `0` if omitted.
     * `filter.frequency.max` - the maximum document frequency (inclusive) in order to be loaded in to memory. Either a percentage if < `1.0` or an absolute value. `0` if omitted.
     * `filter.frequency.min_segment_size` - the minimum number of documents in a segment in order for the filter to be applied. Small segments might be omitted with this setting.

    ## Regular Expression Filter

    The regular expression filter applies a regular expression to each term  during loading and only loads terms into memory that match the given regular expression.

    Here is an example mapping:

    ```json
    {
        "tweet" : {
            "properties" : {
                "locale" : {
                    "type" : "string",
                    "fielddata" : "format=paged_bytes;filter.regex=^en_.*",
                    "index" : "analyzed",
                }
            }
        }
    }
    ```

    Closes #2874