commit eb8ea6362698a08378e7eea6841b70e0132b81a4
Author: Christoph Büscher <christoph@elastic.co>
Date:   Wed Sep 23 16:40:55 2015 +0200

    Query Refactoring: remove deprecated methods and temporary classes

    After all queries now have a `toQuery` method and the parsers all
    support `fromXContent` it is possible to remove the following
    workarounds and deprecated methods we kept around while doing the
    refactoring:

    * remove the BaseQueryParser and BaseQueryParserTemp. All parsers
      implement QueryParser directly now
    * remove deprecated methods in QueryParseContext that either returned
      a Query or a Filter.
    * remove the temporary QueryWrapperQueryBuilder

    Relates to #10217