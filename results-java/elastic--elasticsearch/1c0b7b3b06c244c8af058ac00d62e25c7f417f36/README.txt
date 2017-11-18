commit 1c0b7b3b06c244c8af058ac00d62e25c7f417f36
Author: Christoph Büscher <christoph@elastic.co>
Date:   Fri Jun 12 16:46:18 2015 +0200

    Query refactoring: ConstantScoreQueryBuilder and Parser

    Moving the query building functionality from the parser to the builders
    new toQuery() method analogous to other recent query refactorings.

    Relates to #10217
    Closes #11629