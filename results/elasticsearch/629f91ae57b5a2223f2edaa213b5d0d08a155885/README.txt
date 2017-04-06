commit 629f91ae57b5a2223f2edaa213b5d0d08a155885
Author: Adrien Grand <jpountz@gmail.com>
Date:   Fri Jul 18 17:02:07 2014 +0200

    Fielddata: goodbye comparators.

    This commit removes custom comparators in favor of the ones that are in Lucene.

    The major change is for nested documents: instead of having a comparator wrapper
    that deals with nested documents, this is done at the fielddata level by having
    a selector that returns the value to use for comparison.

    Sorting with custom missing string values might be slower since it is using
    TermValComparator since Lucene's TermOrdValComparator only supports sorting
    missing values first or last. But other than this particular case, this change
    will allow us to benefit from improvements on comparators from the Lucene side.

    Close #5980