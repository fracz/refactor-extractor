commit 350e3a485094cf57113f1c8d340c68e536a733b5
Merge: a40587b ff46303
Author: Christoph Büscher <christoph@elastic.co>
Date:   Mon Mar 7 16:11:55 2016 +0100

    Merge pull request #16955 from cbuescher/timezone-dateRangeAgg

    Date range aggregations used to be unable to use a `time_zone` parameter to e.g. be applied
    int date math roundings like in `now/d` (see #10130 as an example). After the aggregation
    refactoring, the time_zone parameter has been pulled up to ValuesSourceAggregatorBuilder
    and can now be used in date range aggregations as well.

    This change adds randomized time zone settings to the existing IT tests to verify that the
    `time_zone` parameter is honored when calculating the bucket boundaries. Also moving
    the DateRangeTests from module-groovy/messy back to core as DateRangeIT, sharing
    common script mocks with DateHistogramIT and adding documentation for the
    `time_zone` parameter in the date range aggregation docs.

    Closes #10130