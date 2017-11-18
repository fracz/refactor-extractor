commit a1cb713ab44eadfda620877141f0eb0c2a341823
Author: Dan LaRocque <dalaro@hopcount.org>
Date:   Tue Jun 4 13:30:39 2013 -0400

    Created wrapper KCVS that instruments w/ Metrics

    MetricsInstrumentedStore wraps the KeyColumnValueStore provided to its
    constructor with Metrics Timers, Counters, and Histograms to track
    basic statistics about method runtime, result sizes, and exception
    rate.

    This commit also refactors Metrics configuration in MetricManager and
    adds graphdb configuration options to control each of the four
    supported Metrics reporters.