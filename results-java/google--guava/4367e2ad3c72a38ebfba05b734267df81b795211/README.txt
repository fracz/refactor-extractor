commit 4367e2ad3c72a38ebfba05b734267df81b795211
Author: Charles Fry <fry@google.com>
Date:   Thu Mar 29 17:21:46 2012 -0400

    Use SortedMapTestSuiteBuilder and NavigableMapTestSuiteBuilder more widely.
    This requires two improvements:
    - Generalize NullsBeforeB, intended for use with test elements "a", "b", "c", etc., to introduce NullsBeforeTwo, intended for use with test elements "one", "two", "three", ....
    - Do not require SortedMap keySet() to be a SortedSet (as the interface doesn't require this).
    -------------
    Created by MOE: http://code.google.com/p/moe-java
    MOE_MIGRATED_REVID=28826718