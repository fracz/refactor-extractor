commit 2754aaa65ca8d5b84f2cdca73eb8e23dc1230489
Author: Wojciech Biela <Wojciech.Biela@teradata.com>
Date:   Tue Feb 2 11:00:05 2016 +0100

    Avg aggregate function for decimal(p,s)

    This is the first of the aggregate functions for parametric types.
    Implemented using the "old" way, via specialize() not annotations, since
    that "new" mechanism doesn't support parametric types for aggregates
    yet. Probably to be refactored after improving latter.

    This implementation follows the other aggregate functions implementation
    patterns (like having the state object initialized in the function, etc).

    The return value of the "avg" function is a decimal with the scale and
    precision of the input column. The implementation uses a BigInteger
    internally as the accumulator.