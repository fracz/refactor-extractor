commit 89a9115e321b0562f8f84da79c5b8754b5601d18
Author: Adrian Kelly <adrian@gradle.com>
Date:   Mon Oct 19 14:43:49 2015 +0100

    Logical refactor for unmanaged collection type check

    - Do not rely on the schema type to determine an unmanaged collection
    - Fix typo in method name

    +review REVIEW-5657