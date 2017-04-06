commit 91b97a6f7bda843a030e5c0cefacfcd2b5be8e2c
Author: Isabel Drost-Fromm <isabel.drostfromm@elasticsearch.com>
Date:   Mon Jun 29 09:32:56 2015 +0200

    Refactors GeoBoundingBoxQueryBuilder/-Parser

    This add equals, hashcode, read/write methods, validation, separates toQuery
    and JSON parsing and adds serialization and query generation tests.

    Deprecates two types of initializing the bounding box: In our documentation we
    speak about specifying top/left and bottom/right corner of a bounding box. Here
    we also allow for top/right and bottom/left. This adds not only to the amount
    of code but also testing needed w/o too much benefit for the user other than
    more chances to confuse top/right/bottom/left/latitude/longitude IMHO.

    Missing: The toQuery method with type set to "indexed" is not tested at the
    moment.

    Cleanup changes unrelated to base refactoring:
    * Switched from type String to enum for types in GeoBoundingBoxQueryBuilder.
    * Switched to using type GeoPoint for storing the bounding box coordinates
      instead of array of double values.

    Relates to #10217 for the query refactoring part.
    Relates to #12016 for how missing mappings are handled.

    Adds a utility class for generating random geo data.

    Adds some missing documentation.

    Extend test to MEMORY type config

    Fix final review comments and rebase