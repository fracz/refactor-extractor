commit 12d9268db256b728e8d334801739fb46167902b6
Author: Adrien Grand <jpountz@gmail.com>
Date:   Mon Jul 8 16:12:12 2013 +0200

    Make field data able to support more than 2B ordinals per segment.

    Although segments are limited to 2B documents, there is not limit on the number
    of unique values that a segment may store. This commit replaces 'int' with
    'long' every time a number is used to represent an ordinal and modifies the
    data-structures used to store ordinals so that they can actually support more
    than 2B ordinals per segment.

    This commit also improves memory usage of the multi-ordinals data-structures
    and the transient memory usage which is required to build them (OrdinalsBuilder)
    by using Lucene's PackedInts data-structures. In the end, loading the ordinals
    mapping from disk may be a little slower, field-data-based features such as
    faceting may be slightly slower or faster depending on whether being nicer to
    the CPU caches balances the overhead of the additional abstraction or not, and
    memory usage should be better in all cases, especially when the size of the
    ordinals mapping is not negligible compared to the size of the values (numeric
    data for example).

    Close #3189