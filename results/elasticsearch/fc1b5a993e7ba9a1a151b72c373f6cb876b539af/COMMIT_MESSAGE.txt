commit fc1b5a993e7ba9a1a151b72c373f6cb876b539af
Author: Christoph Büscher <christoph@elastic.co>
Date:   Wed Jul 22 12:44:47 2015 +0200

    Query refactoring: SpanWithinQueryBuilder and Parser

    Moving the query building functionality from the parser to the builders
    new toQuery() method analogous to other recent query refactorings.

    Relates to #10217