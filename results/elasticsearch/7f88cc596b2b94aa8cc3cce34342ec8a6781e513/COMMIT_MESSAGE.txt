commit 7f88cc596b2b94aa8cc3cce34342ec8a6781e513
Author: Christoph Büscher <christoph@elastic.co>
Date:   Mon Jul 20 18:39:45 2015 +0200

    Query refactoring: SpanNotQueryBuilder and Parser

    Moving the query building functionality from the parser to the builders
    new doToQuery() method analogous to other recent query refactorings.

    Relates to #10217
    Closes #12365