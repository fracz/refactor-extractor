commit 885b258871b678684910a11e7f4adcd1a69ad34b
Author: Christoph Büscher <christoph@elastic.co>
Date:   Fri Jul 10 16:29:22 2015 +0200

    Query refactoring: SpanNearQueryBuilder and Parser

    Moving the query building functionality from the parser to the builders
    new toQuery() method analogous to other recent query refactorings.

    Relates to #10217