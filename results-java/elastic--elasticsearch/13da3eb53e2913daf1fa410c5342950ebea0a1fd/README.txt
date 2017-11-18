commit 13da3eb53e2913daf1fa410c5342950ebea0a1fd
Author: Jim Ferenczi <jim.ferenczi@elastic.co>
Date:   Thu Jul 13 15:32:17 2017 +0200

    Refactor QueryStringQuery for 6.0 (#25646)

    This change refactors the query_string query to analyze the query text around logical operators of the query string the same way than a match_query/multi_match_query.
    It also adds a type parameter that can be used to change the way multi fields query are built the same way than a multi_match query does.

    Now that these queries share the same behavior regarding text analysis, some parameters are obsolete and have been deprecated:

    split_on_whitespace: This setting is now ignored with a deprecation notice
    if it is used explicitely. With this PR The query_string always splits on logical operator.
    It simplifies the understanding of the other parameters that can have different meanings
    depending on the value of split_on_whitespace.

    auto_generate_phrase_queries: This setting is now ignored with a deprecation notice
    if it is used explicitely. This setting only makes sense when the parser splits on whitespace.

    use_dismax: This setting is now ignored with a deprecation notice
    if it is used explicitely. The tie_breaker parameter is sufficient to handle best_fields/most_fields.

    Fixes #25574