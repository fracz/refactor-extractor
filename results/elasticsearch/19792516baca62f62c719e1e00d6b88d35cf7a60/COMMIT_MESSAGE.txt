commit 19792516baca62f62c719e1e00d6b88d35cf7a60
Author: Colin Goodheart-Smithe <colings86@users.noreply.github.com>
Date:   Mon Sep 7 11:00:53 2015 +0100

    Query Refactoring: Refactor of GeoDistanceRangeQuery

    Moving the query building functionality from the parser to the builders
    new toQuery() method analogous to other recent query refactorings.

    Also this PR removes the check that the index is created before 2.0 for the normalize parameter. The parameter is now always parsed but as a deprecated parameter. We cannot and should not access the index version during parsing.

    Relates to #10217

    PR goes against the query-refactoring branch