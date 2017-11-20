commit 2f951d35172037cace0f2a94264117458a4b33f4
Author: lvca <l.garulli@gmail.com>
Date:   Thu Oct 8 02:46:31 2015 +0200

    First implementation of binary queries

    Current status, limitations and considerations:
    - I did other optimizations in the binary protocol and benchmark suite
    shows an improvement in almost all the benchmarks
    - Now the cost of comparing fields is much lower, so queries that
    involve many fields will be even faster
    - The binary comparator is able to do also conversions in the most
    efficient way. So you can binary compare an Integer with a Long
    - Only EQUALS has been implemented. Even if this is the most common use
    case, it should be easy to create a compare() to accept >, <, >= and <=
    - COLLATE is not supported. I'd like to move the collate strategy in
    the new comparator, so we optimize also this as much as we can
    - I tried to avoid breaking the SQL engine too much, so the
    implementation needs to be adapted to the new executor, when ready