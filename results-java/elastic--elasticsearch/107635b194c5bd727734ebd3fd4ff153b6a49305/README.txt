commit 107635b194c5bd727734ebd3fd4ff153b6a49305
Author: Christoph Büscher <christoph@elastic.co>
Date:   Tue Jun 16 17:58:49 2015 +0200

    Query refactoring: FieldMaskingSpanQueryBuilder and Parser

    Moving the query building functionality from the parser to the builders
    new toQuery() method analogous to other recent query refactorings.

    Relates to #10217
    Closes #11717