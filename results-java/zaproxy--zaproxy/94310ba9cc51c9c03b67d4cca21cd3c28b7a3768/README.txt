commit 94310ba9cc51c9c03b67d4cca21cd3c28b7a3768
Author: thc202 <thc202@gmail.com>
Date:   Fri Mar 18 19:39:46 2016 +0000

    Fix (non-local IP address) ZAP API loop

    Check if the requested address belongs to any of the network interfaces
    when checking if it is recursive, that is, a request to ZAP itself,
    preventing a request loop when the IP address is non-local.

    Add the check to method ProxyThread.isRecursive(HttpRequestHeader) and
    refactor, to reduce code complexity, by extracting simpler methods.

    Fix #2318 - ZAP Error [java.net.SocketTimeoutException]: Read timed out
    when running on AWS EC2 instance