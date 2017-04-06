commit 966c02b1ac441381b426bcd63835570ea36493f8
Author: Christoph Büscher <christoph@elastic.co>
Date:   Wed Jul 8 10:56:50 2015 +0200

    Query refactoring: SpanMultiTermQueryBuilder and Parser

    Moving the query building functionality from the parser to the builders
    new toQuery() method analogous to other recent query refactorings.

    Relates to #10217
    Closes #12182