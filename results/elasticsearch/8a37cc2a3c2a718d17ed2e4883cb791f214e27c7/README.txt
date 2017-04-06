commit 8a37cc2a3c2a718d17ed2e4883cb791f214e27c7
Author: Christoph Büscher <christoph@elastic.co>
Date:   Fri May 22 17:07:39 2015 +0200

    Query refactoring: Adding getBuilderPrototype() method to all QueryParsers

    Currently there is a registry for all QueryParsers accessible via the IndicesQueriesModule.
    For deserializing nested queries e.g. for the BoolQueryBuilder (#11121) we need to look up
    query builders by their name to be able to deserialize using a prototype builder of the concrete
    class. This PR adds a getBuilderPrototype() method to each query parser so we can re-use the
    parser registry to get the corresponding builder using the query name.

    Closes #11344