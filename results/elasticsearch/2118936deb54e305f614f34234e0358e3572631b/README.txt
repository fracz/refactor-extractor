commit 2118936deb54e305f614f34234e0358e3572631b
Author: Colin Goodheart-Smithe <colings86@users.noreply.github.com>
Date:   Wed Sep 9 16:45:10 2015 +0100

    Query Refactoring: Refactor of GeoShapeQuery

    Moving the query building functionality from the parser to the builders
    new toQuery() method analogous to other recent query refactorings.

    Relates to #10217

    PR goes against the query-refactoring branch