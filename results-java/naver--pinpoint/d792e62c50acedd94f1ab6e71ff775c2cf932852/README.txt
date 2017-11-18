commit d792e62c50acedd94f1ab6e71ff775c2cf932852
Author: koo.taejin <koo.taejin@navercorp.com>
Date:   Wed May 4 16:36:49 2016 +0900

    Support the async put API provided by HTableMultiplexer to improve the write throughput in collector. #1683

     - support to reuse connection
     - added HBaseAsyncOperationMetric to CollectorMetric
     - show  HBaseAsyncOperationMetric to use JMX