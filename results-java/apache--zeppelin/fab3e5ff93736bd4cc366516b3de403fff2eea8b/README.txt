commit fab3e5ff93736bd4cc366516b3de403fff2eea8b
Author: Bruno Bonnin <bbonnin@gmail.com>
Date:   Wed Oct 12 23:20:25 2016 +0200

    [ZEPPELIN-1537] Elasticsearch improvement for results of aggregations

    ### What is this PR for?
    The result of an aggregation query returned by the interpreter contains only "key" and "doc_count" in case of a multi-buckets aggregations.
    But the result returned by Elasticsearch can contain more data according to the query.
    This PR is an improvement of the result returned by the interpreter.

    ### What type of PR is it?
    [Improvement]

    ### Todos
    * [X] - Dev of the improvement in the interpreter
    * [X] - Add a test case

    ### What is the Jira issue?
    https://issues.apache.org/jira/browse/ZEPPELIN-1537

    ### How should this be tested?
    In a paragraph, enter a query with multiple aggregations:
    search /logs { "aggs" : {
                "length" : { "terms": { "field": "status" },
                "aggs" : { "sum_length" : { "sum" : { "field" : "content_length" } } } } }

    ### Screenshots (if appropriate)

    ### Questions:
    * Does the licenses files need update? No
    * Is there breaking changes for older versions? No
    * Does this needs documentation? No

    Author: Bruno Bonnin <bbonnin@gmail.com>

    Closes #1508 from bbonnin/master and squashes the following commits:

    a0a7bb9 [Bruno Bonnin] Elasticsearch improvement for results of aggregations