commit a3c294d4e9415445ebb451aff2433a99efe2c4a6
Author: Christoph Büscher <christoph@elastic.co>
Date:   Wed Aug 5 19:19:45 2015 +0200

    Move flag to track filter context to QueryShardContext

    Currently there is a flag in the QueryParseContext that keeps track
    of whether an inner query sits inside a filter and should therefore
    produce an unscored lucene query. This is done in the
    parseInnerFilter...() methods that are called in the fromXContent()
    methods or the parse() methods we haven't refactored yet. This needs
    to move to the toQuery() method in the refactored builders, since the
    query builders themselves have no information about the parent query
    they might be nested in.

    This PR moves the isFilter flag from the QueryParseContext to the re-
    cently introduces QueryShardContext. The parseInnerFilter... methods
    need to stay in the QueryParseContext for now, but they already delegate
    to the flag that is kept in QueryShardContext. For refactored queries
    (like BoolQueryBuilder) references to isFilter() are moved from the
    parser to the corresponding builder. Builders where the inner query
    was previously parsed using parseInnerFilter...() now use a newly
    introduces toFilter(shardContext) method that produces the nested lucene
    query with the filter context flag switched on.

    Closes #12731