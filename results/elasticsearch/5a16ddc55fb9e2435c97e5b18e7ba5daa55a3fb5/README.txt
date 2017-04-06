commit 5a16ddc55fb9e2435c97e5b18e7ba5daa55a3fb5
Author: Christoph Büscher <christoph@elastic.co>
Date:   Fri Jun 12 14:56:48 2015 +0200

    Query refactoring: AndQueryBuilder and Parser

    Moving the query building functionality from the parser to the builders
    new toQuery() method analogous to other recent query refactorings.

    Relates to #10217
    Closes #11628