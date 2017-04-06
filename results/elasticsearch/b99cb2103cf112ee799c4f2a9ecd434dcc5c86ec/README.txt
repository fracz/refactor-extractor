commit b99cb2103cf112ee799c4f2a9ecd434dcc5c86ec
Author: Christoph Büscher <christoph@elastic.co>
Date:   Mon May 18 12:17:55 2015 +0200

    Query Refactoring: Add RangeQueryBuilder and Parser refactoring and test.

    Split the parse(QueryParseContext ctx) method into a parsing and a query building part,
    adding Streamable support for serialization and hashCode(), equals() for better testing.

    This PR also adds test setup for two mappes fields (integer, date) to the BaseQueryTestCase
    and introduces helper methods for optional conversion of String fields to BytesRef representation
    that is shared with the already refactored BaseTermQueryBuilder.

    Relates to #10217
    Closes #11108