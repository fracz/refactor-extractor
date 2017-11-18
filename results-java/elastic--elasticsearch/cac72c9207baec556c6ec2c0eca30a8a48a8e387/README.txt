commit cac72c9207baec556c6ec2c0eca30a8a48a8e387
Author: Colin Goodheart-Smithe <colings86@users.noreply.github.com>
Date:   Tue Sep 8 15:18:22 2015 +0100

    Query Refactoring: Refactor of GeoPolygonQuery

    Moving the query building functionality from the parser to the builders
    new toQuery() method analogous to other recent query refactorings.

    Relates to #10217

    PR goes against the query-refactoring branch