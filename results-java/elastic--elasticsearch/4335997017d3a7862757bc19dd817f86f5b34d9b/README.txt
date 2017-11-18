commit 4335997017d3a7862757bc19dd817f86f5b34d9b
Author: Colin Goodheart-Smithe <colings86@users.noreply.github.com>
Date:   Mon Mar 21 11:04:31 2016 +0000

    Aggregations: Fixes the defaults for `keyed` in the percentiles aggregations

    During the aggregation refactoring the default value for `keyed` in the `percentiles` and `percentile_ranks` aggregation was inadvertently changed from `true` to `false`. This change reverts the defaults to the old (correct) value