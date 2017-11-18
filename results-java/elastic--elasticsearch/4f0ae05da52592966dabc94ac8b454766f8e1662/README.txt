commit 4f0ae05da52592966dabc94ac8b454766f8e1662
Author: Colin Goodheart-Smithe <colings86@users.noreply.github.com>
Date:   Tue Sep 8 13:00:07 2015 +0100

    Query Refactoring: Refactor of GeohashCellQuery

    Moving the query building functionality from the parser to the builders
    new toQuery() method analogous to other recent query refactorings.

    Relates to #10217

    PR goes against the query-refactoring branch