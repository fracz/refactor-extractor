commit 4131bbf861b789da3d318df93af13401f76ee2dc
Author: koo.taejin <koo.taejin@navercorp.com>
Date:   Fri Apr 15 16:54:24 2016 +0900

    Support the async put API provided by HTableMultiplexer to improve the write throughput in collector. #1683

     - If asyncOperation is not set, then execute put method instead of asyncPut method.
     - wait destory method until async queue is empty.