commit 028ff7e49eeb6d0be97233a6dc915627356d8839
Author: javanna <cavannaluca@gmail.com>
Date:   Fri May 15 20:15:53 2015 +0200

    Query refactoring: QueryBuilder to extend Writeable

    `QueryBuilder`s need to become streamable over the wire, so we can use them as our own intermediate query representation and send it over the wire. Using `Writeable` rather than `Streamable` allows us to make some fields `final` and delete default constructors needed only for serialization purposes.

    Taken the chance also to revise the internals of `IdsQueryBuilder` (modified some internal data type and added deduplication of ids to reduce bits to serialize). Expanded also `IdsQueryBuilderTest`, injected random types and improved comparison.

    Moved all query builder tests to leverage lucene's equals and hashcode to compare result of `toQuery` with expected lucene query

    Also refactored spanterm and term query tests, more core could be shared.

    Also changed `RangeQueryBuilderTest` to remove the need for query rewriting and added some more coverage (we don't test when search context is null only anymore, search context gets now randomly set)

    Closes #11191