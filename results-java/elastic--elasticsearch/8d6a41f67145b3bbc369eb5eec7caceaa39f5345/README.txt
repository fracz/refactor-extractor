commit 8d6a41f67145b3bbc369eb5eec7caceaa39f5345
Author: Adrien Grand <jpountz@gmail.com>
Date:   Tue Feb 14 16:05:19 2017 +0100

    Nested queries should avoid adding unnecessary filters when possible. (#23079)

    When nested objects are present in the mappings, many queries get deoptimized
    due to the need to exclude documents that are not in the right space. For
    instance, a filter is applied to all queries that prevents them from matching
    non-root documents (`+*:* -_type:__*`). Moreover, a filter is applied to all
    child queries of `nested` queries in order to make sure that the child query
    only matches child documents (`_type:__nested_path`), which is required by
    `ToParentBlockJoinQuery` (the Lucene query behing Elasticsearch's `nested`
    queries).

    These additional filters slow down `nested` queries. In 1.7-, the cost was
    somehow amortized by the fact that we cached filters very aggressively. However,
    this has proven to be a significant source of slow downs since 2.0 for users
    of `nested` mappings and queries, see #20797.

    This change makes the filtering a bit smarter. For instance if the query is a
    `match_all` query, then we need to exclude nested docs. However, if the query
    is `foo: bar` then it may only match root documents since `foo` is a top-level
    field, so no additional filtering is required.

    Another improvement is to use a `FILTER` clause on all types rather than a
    `MUST_NOT` clause on all nested paths when possible since `FILTER` clauses
    are more efficient.

    Here are some examples of queries and how they get rewritten:

    ```
    "match_all": {}
    ```

    This query gets rewritten to `ConstantScore(+*:* -_type:__*)` on master and
    `ConstantScore(_type:AutomatonQuery {\norg.apache.lucene.util.automaton.Automaton@4371da44})`
    with this change. The automaton is the complement of `_type:__*` so it matches
    the same documents, but is faster since it is now a positive clause. Simplistic
    performance testing on a 10M index where each root document has 5 nested
    documents on average gave a latency of 420ms on master and 90ms with this change
    applied.

    ```
    "term": {
      "foo": {
        "value": "0"
      }
    }
    ```

    This query is rewritten to `+foo:0 #(ConstantScore(+*:* -_type:__*))^0.0` on
    master and `foo:0` with this change: we do not need to filter nested docs out
    since the query cannot match nested docs. While doing performance testing in
    the same conditions as above, response times went from 250ms to 50ms.

    ```
    "nested": {
      "path": "nested",
      "query": {
        "term": {
          "nested.foo": {
            "value": "0"
          }
        }
      }
    }
    ```

    This query is rewritten to
    `+ToParentBlockJoinQuery (+nested.foo:0 #_type:__nested) #(ConstantScore(+*:* -_type:__*))^0.0`
    on master and `ToParentBlockJoinQuery (nested.foo:0)` with this change. The
    top-level filter (`-_type:__*`) could be removed since `nested` queries only
    match documents of the parent space, as well as the child filter
    (`#_type:__nested`) since the child query may only match nested docs since the
    `nested` object has both `include_in_parent` and `include_in_root` set to
    `false`. While doing performance testing in the same conditions as above,
    response times went from 850ms to 270ms.