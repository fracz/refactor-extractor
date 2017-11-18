commit 0c7eb0fddce92f81cf1c04508621d617919be455
Author: Christoph Büscher <christoph@elastic.co>
Date:   Fri May 8 15:19:52 2015 +0200

    Query refactoring: refactored IdsQueryBuilder and Parser and added test

    Split the parse(QueryParseContext ctx) method into a parsing and a query building part,
    adding Streamable for serialization and hashCode(), equals() for better testing.
    Add basic unit test for Builder and Parser.

    Closes #10670