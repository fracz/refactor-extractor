commit de41981373995dfbe815429525d547d1bc7bd443
Author: Adrien Grand <jpountz@gmail.com>
Date:   Tue Jan 27 14:22:01 2015 +0100

    Aggs: Refactor aggregations to use lucene5-style collectors.

    Aggregators now return a new collector instance per segment, like Lucene 5 does
    with its oal.search.Collector API. This is important for us because things like
    knowing whether the field is single or multi-valued is only known at a segment
    level.

    In order to do that I had to change aggregators to notify their sub aggregators
    of new incoming segments (pretty much in the spirit of #6477) while everything
    used to be centralized in the AggregationContext class. While this might slow
    down a bit deeply nested aggregation trees, this also makes the children
    aggregation and the `breadth_first` collection mode much better options since
    they can now only replay what they need while they used to have to replay the
    whole aggregation tree.

    I also took advantage of this big refactoring to remove some abstractions that
    were not really required like ValuesSource.MetaData or BucketAnalysisCollector.
    I also splitted Aggregator into Aggregator and AggregatorBase in order to
    separate the Aggregator API from implementation helpers.

    Close #9544