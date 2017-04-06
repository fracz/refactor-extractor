commit a4a4f19d13c8a76e84dde5fa70dd8f1c83a878c2
Author: Christoph Büscher <christoph.buescher@elasticsearch.com>
Date:   Mon Apr 13 18:50:03 2015 +0200

    Query refactoring: Introduce toQuery() and fromXContent() in QueryBuilders and QueryParsers

    The planed refactoring of search queries layed out in #9901 requires to split the "parse()"
    method in QueryParsers into two methods, first a "fromXContent(...)" method that allows parsing
    to an intermediate query representation (currently called FooQueryBuilder) and second a
    "Query toQuery(...)" method on these intermediate representations that create the actual lucene queries.

    This PR is a first step in that direction as it introduces the interface changes necessary for the further
    refactoring. It introduces the new interface methods while for now keeping the old Builder/Parsers still
    in place by delegating the new "toQuery()" implementations to the existing "parse()" methods, and by
    introducing a "catch-all" "fromXContent()" implementation in a BaseQueryParser that returns a temporary
    QueryBuilder wrapper implementation. This allows us to refactor the existing QueryBuilders step by step
    while already beeing able to start refactoring queries with nested inner queries.

    Closes #10580