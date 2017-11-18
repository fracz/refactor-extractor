commit b5967fc5a14b9a733be43a4771175eace8a856bd
Author: Brett Wooldridge <brett.wooldridge@gmail.com>
Date:   Thu Aug 7 16:19:59 2014 +0900

    Significant refactor of pool internals.  Connections are now always wrapped in new proxies in getConnection().