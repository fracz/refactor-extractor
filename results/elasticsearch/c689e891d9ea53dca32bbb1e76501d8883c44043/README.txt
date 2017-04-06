commit c689e891d9ea53dca32bbb1e76501d8883c44043
Author: Christoph Büscher <christoph@elastic.co>
Date:   Wed Jul 8 18:25:16 2015 +0200

    Query refactoring: SpanNearQueryBuilder and Parser

    Moving the query building functionality from the parser to the builders
    new toQuery() method analogous to other recent query refactorings.

    Relates to #10217