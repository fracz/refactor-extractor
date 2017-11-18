commit aadfd1794c4e50356e8f56c6db20a8e7f1a654a8
Author: Christoph Büscher <christoph@elastic.co>
Date:   Tue Jun 9 12:17:39 2015 +0200

    Query refactoring: refactored LimitQueryBuilder and Parser and added test

    Split the parse method into a parsing and a query building part, adding serialization
    and hashCode(), equals() for better testing. Add basic unit test for Builder and Parser.

    Closes #11551