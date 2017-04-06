commit e14c7a49424bcf41a2f0be5af1ccfedb8761a980
Author: Christoph Büscher <christoph.buescher@elasticsearch.com>
Date:   Tue May 5 23:18:36 2015 +0200

    Query refactoring: TermQueryBuilder refactoring and test

    Also extended BaseQueryTestCase so it has helper methods for parsing the query header and
    extended the toQuery() test method so it passes down parse context to sublass to make
    assertions on side effects calling toQuery() has on the parseContext.

    Added validate() method to QueryBuilder and BaseQueryBuilder that can be used to validate
    a query and that returns a QueryValidationException containing all collected validation
    errors.