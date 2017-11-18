commit a065b80e5787729b4c109ce6f5c8d18d77409260
Author: Dave Syer <dsyer@pivotal.io>
Date:   Mon May 18 15:00:15 2015 +0100

    Add MetricsEndpointMetricReader for exporting all metrics

    User can add a bean of type MetricsEndpointMetricReader to opt in
    to exporting all metrics via the MetricsEndpoint (instead of via
    MetricReaders). There are disadvantages (like no accurate timestamps)
    so it's best to leave it as an opt in.

    Also improved tests for metric auto configuration a bit.