commit 1908d6dc7366ae1a72e1d0390470622ceca5ecda
Author: Christoph Büscher <christoph@elastic.co>
Date:   Tue Mar 15 11:01:09 2016 +0100

    Add build() method to SortBuilder implementations

    For the current refactoring of SortBuilders related to #10217,
    each SortBuilder should get a build() method that produces a
    SortField according to the SortBuilder parameters on the shard.

    This change also slightly refactors the current parse method in
    SortParseElement to extract an internal parse method that returns
    a list of sort fields only needs a QueryShardContext as input
    instead of a full SearchContext. This allows using this internal
    parse method for testing.