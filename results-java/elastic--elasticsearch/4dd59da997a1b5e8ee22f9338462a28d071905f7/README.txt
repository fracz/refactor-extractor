commit 4dd59da997a1b5e8ee22f9338462a28d071905f7
Author: Christoph Büscher <christoph@elastic.co>
Date:   Mon Jul 6 17:17:51 2015 +0200

    Query refactoring: SpanContainingQueryBuilder and Parser

    Moving the query building functionality from the parser to the builders
    new toQuery() method analogous to other recent query refactorings.

    Relates to #10217