commit 6f8c0ce9667840cfd5fcc291088f395f00e23dba
Author: Christoph Büscher <christoph@elastic.co>
Date:   Wed Jun 17 18:59:44 2015 +0200

    Query refactoring: OrQueryBuilder and Parser

    Moving the query building functionality from the parser to the builders
    new toQuery() method analogous to other recent query refactorings.

    Relates to #10217