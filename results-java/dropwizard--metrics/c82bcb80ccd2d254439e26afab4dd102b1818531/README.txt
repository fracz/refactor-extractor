commit c82bcb80ccd2d254439e26afab4dd102b1818531
Author: Mårten Gustafson <marten.gustafson@gmail.com>
Date:   Wed Nov 16 16:53:43 2011 +0100

    Refactor how reporters interact with metrics.

    = Origin
    The core of this refactoring is the introduction of the MetricsProcessor
    interface which now all the reporters implement. The primary driver here
    is to eliminate a slew of code blocks following this pattern:

        if(metric instanceof Timer) {
            ...
        } else if(metric instanceof Counter) {
            ...
        }
        ...etc...

    So there's now a MetricsProcessor that solves this and forces it's
    implementors to handle all different metric types. An effect of this is that
    introducing a new type of metric will trickle down to all reporters and
    implementors of this is interface.

    = Collateral changes
    In the process of getting this in place the following has also happened:
    - New interface: "Summarized" that groups min(), max(), mean(), stdDev()
    - New interface: "Percentiled" that groups percentile(double) and
      percentiles(Double...)
    - A lot of tests for reporters have been added (thanks to @jebl01)
    - Utils.sortAndFilterMetrics() now returns the MetricName in its result
    - Added Utils.filterMetrics() to only filter based on a MetricPredicate

    The two new interfaces also allowed elimination of duplication in some
    of the reporters.