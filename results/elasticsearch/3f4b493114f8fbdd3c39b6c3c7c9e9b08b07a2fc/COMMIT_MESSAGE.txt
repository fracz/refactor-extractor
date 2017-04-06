commit 3f4b493114f8fbdd3c39b6c3c7c9e9b08b07a2fc
Author: Christoph Büscher <christoph@elastic.co>
Date:   Thu May 28 16:34:49 2015 +0200

    Query Refactoring: DisMaxQueryBuilder and Parser

    Moving the query building functionality from the parser to the builders
    new toQuery() method analogous to other recent query refactorings.

    Relates to #10217
    Closes #11703