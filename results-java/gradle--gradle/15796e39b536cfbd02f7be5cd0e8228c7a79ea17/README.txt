commit 15796e39b536cfbd02f7be5cd0e8228c7a79ea17
Author: Daz DeBoer <darrell.deboer@gradleware.com>
Date:   Wed Dec 3 11:22:16 2014 -0700

    Minor refactoring to NativeBinary.NativeBinaryTasks sub-interfaces

    - Renamed to TasksCollection
    - Made implementations private where possible
    - Use specific Link task types

    +review REVIEW-5268