commit b6cdc46a6193a5784e87eff8f2f84df682f5997e
Author: Christoph Büscher <christoph@elastic.co>
Date:   Wed Jun 17 13:48:52 2015 +0200

    Query refactoring: QueryFilterBuilder and Parser

    Moving the query building functionality from the parser to the builders
    new toQuery() method analogous to other recent query refactorings. In
    this case this also includes FQueryFilterParser, since both queries are
    closely related.

    Relates to #10217
    Closes #11729