commit 29d16cd1e83c215c61e49f8902ad731fd7ff4dc4
Author: Alex Ksikes <alex.ksikes@gmail.com>
Date:   Tue May 26 12:00:06 2015 +0200

    Refactors CommonTermsQuery

    Refactors CommonTermsQuery analogous to TermQueryBuilder. Still left to do are
    the tests to compare between builder and actual Lucene query.

    Relates to #10217

    This PR is against the query-refactoring branch.