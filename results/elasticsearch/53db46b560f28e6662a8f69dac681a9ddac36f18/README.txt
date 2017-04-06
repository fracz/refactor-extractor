commit 53db46b560f28e6662a8f69dac681a9ddac36f18
Author: Christoph Büscher <christoph@elastic.co>
Date:   Tue Jul 7 09:13:28 2015 +0200

    Query refactoring: SpanFirstQueryBuilder and Parser

    Moving the query building functionality from the parser to the builders
    new toQuery() method analogous to other recent query refactorings.

    Relates to #10217