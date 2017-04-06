commit 904cbf53409ae62bb67cd07c31ef88121cff9361
Author: Christoph Büscher <christoph@elastic.co>
Date:   Mon Jul 27 18:32:30 2015 +0200

    Separating QueryParseContext and QueryShardContext

    We are currently splitting the parse() method in the query parsers into a
    query parsing part (fromXContent(QueryParseContext)) and a new method that
    generates the lucene query (toQuery(QueryParseContext)). At some point we
    would like to be able to excute these two steps in different phases, one
    on the coordinating node and one on the shards.

    This PR tries to anticipate this by renaming the existing QueryParseContext
    to QueryShardContext to make it clearer that this context object will provide
    the information needed to generate the lucene queries on the shard. A new
    QueryParseContext is introduced and all the functionality needed for parsing
    the XContent in the request is moved there (parser, helper methods for parsing
    inner queries).

    While the refactoring is not finished, the new QueryShardContext will expose the
    QueryParseContext via the parseContext() method. Also, for the time beeing the
    QueryParseContext will contain a link back to the QueryShardContext it wraps, which
    eventually will be deleted once the refactoring is done.

    Closes #12527
    Relates to #10217