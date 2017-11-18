commit dc900a08a6c6f346656105fa0e2484ed186f8461
Author: javanna <cavannaluca@gmail.com>
Date:   Mon Oct 26 12:18:36 2015 +0100

    Remove "query" query and fix related parsing bugs

    We have two types of parse methods for queries: one for the inner query, to be used once the parser is positioned within the query element, and one for the whole query source, including the query element that wraps the actual query.

    With the search refactoring we ended up using the former in count, cat count and delete by query, whereas we should have used the former.  It ends up working properly given that we have a registered (deprecated) query called "query", which used to allow to wrap a filter into a query, but this has the following downsides:
    1) prevents us from removing the deprecated "query" query
    2) we end up supporting a top level query that is not wrapped within a query element (pre 1.0 syntax iirc that shouldn't be supported anymore)

    This commit finally removes the "query" query and fixes the related parsing bugs. We also had some tests that were providing queries in the wrong format, those have been fixed too.

    Closes #13326
    Closes #14304