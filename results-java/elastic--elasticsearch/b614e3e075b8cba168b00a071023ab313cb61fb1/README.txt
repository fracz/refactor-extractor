commit b614e3e075b8cba168b00a071023ab313cb61fb1
Author: Christoph Büscher <christoph@elastic.co>
Date:   Tue Mar 22 18:25:22 2016 +0100

    Switch to using refactored SortBuilder in SearchSourceBuilder and elsewhere

    Switching from using list of BytesReference to real SortBuilder list in
    SearchSourceBuilder, TopHitsAggregatorBuilder and TopHitsAggregatorFactory.
    Removing SortParseElement and related sort parsers.