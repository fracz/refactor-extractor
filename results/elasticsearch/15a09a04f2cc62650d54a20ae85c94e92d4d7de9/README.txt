commit 15a09a04f2cc62650d54a20ae85c94e92d4d7de9
Author: Christoph Büscher <christoph@elastic.co>
Date:   Fri Jul 24 17:05:59 2015 +0200

    Query Refactoring: Move null-checks from constructors and setters to validate

    Following up to #12427, this PR does same changes, moving null-checks from construtors
    and setters in query builder to the validate() method.

    PR against query-refactoring branch