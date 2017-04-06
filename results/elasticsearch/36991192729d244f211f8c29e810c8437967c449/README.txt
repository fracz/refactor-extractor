commit 36991192729d244f211f8c29e810c8437967c449
Author: Christoph Büscher <christoph@elastic.co>
Date:   Thu Jul 23 00:53:23 2015 +0200

    Remove unused QueryParseContext argument in MappedFieldType#rangeQuery()

    The rangeQuery() method in MappedFieldType and some overwriting subtypes takes
    a nullable QueryParseContext argument which is unused and can be deleted.
    This is also useful for the current query parsing refactoring, since
    we want to avoid passing the context object around unnecessarily.