commit 0f63e0a8daec56a9caaa9458ef281cee892093f5
Author: Adrien Grand <jpountz@gmail.com>
Date:   Thu Aug 14 13:46:26 2014 +0200

    Aggregations: Merge LongTermsAggregator and DoubleTermsAggregator.

    These two aggregators basically do exactly the same thing, they just interpret
    bytes differently. This refactoring found an (unreleased) bug in the long terms
    aggregator which didn't work correctly with duplicate values.

    Close #7279