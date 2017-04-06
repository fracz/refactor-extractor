commit 682b499f4640820ae78d29e9c35d5ead8ab4ed98
Author: Christoph Büscher <christoph@elastic.co>
Date:   Wed Jun 17 19:55:25 2015 +0200

    Query refactoring: NotQueryBuilder and Parser

    Moving the query building functionality from the parser to the builders
    new toQuery() method analogous to other recent query refactorings.

    Relates to #10217
    Closes #11823