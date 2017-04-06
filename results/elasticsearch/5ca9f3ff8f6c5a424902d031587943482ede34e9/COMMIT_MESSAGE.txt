commit 5ca9f3ff8f6c5a424902d031587943482ede34e9
Author: Christoph Büscher <christoph@elastic.co>
Date:   Tue Nov 17 18:56:54 2015 +0100

    Geo: Make ShapeBuilders implement Writable

    We recently refactored the queries to make them parsable on the
    coordinating note and adding serialization and equals/hashCode
    capability to them. So far ShapeBuilders nested inside queries
    were still transported as a byte array that needs to be parsed
    later on the shard receiving the query. To be able to also
    serialize geo shapes this way, we also need to make all the
    implementations of ShapeBuilder implement Writable.

    This PR adds this to PointBuilder and also adds tests for
    serialization, equality and hashCode.