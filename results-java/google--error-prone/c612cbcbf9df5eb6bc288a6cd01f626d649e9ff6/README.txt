commit c612cbcbf9df5eb6bc288a6cd01f626d649e9ff6
Author: lowasser <lowasser@google.com>
Date:   Fri Sep 30 18:52:33 2016 -0700

    Lambda inlining improvements

    inline a properly imported and qualified type when the lambda parameter
    type is inferred, and when the original @AfterTemplate leaves the lambda
    type implicit, keep them implicit in the output.

    Previously, Refaster would always inline explicit lambda types, and if
    they were inferred, they would be fully qualified e.g. (java.lang.String
    str) -> str.length.

    MOE_MIGRATED_REVID=134852428