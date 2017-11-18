commit 4b5ae31207359cf453ac220cc8309aded507b8d6
Author: Roman Leventov <leventov@users.noreply.github.com>
Date:   Thu Mar 23 18:23:59 2017 -0600

    QueryMetrics: abstraction layer of query metrics emitting (part of #3798) (#3954)

    * QueryMetrics: abstraction layer of query metrics emitting

    * Minor fixes

    * QueryMetrics.emit() for bulk emit and improve Javadoc

    * Fixes

    * Fix

    * Javadoc fixes

    * Typo

    * Use DefaultObjectMapper

    * Add tests

    * Address PR comments

    * Remove QueryMetrics.userDimensions(); Rename QueryMetric.register() to report()

    * Dedicated TopNQueryMetricsFactory, GroupByQueryMetricsFactory and TimeseriesQueryMetricsFactory

    * Typo

    * More elaborate Javadoc of QueryMetrics

    * Formatting

    * Replace QueryMetric enum with lambdas

    * Add comments and VisibleForTesting annotations