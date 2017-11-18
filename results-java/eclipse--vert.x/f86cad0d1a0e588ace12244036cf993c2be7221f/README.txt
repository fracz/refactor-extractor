commit f86cad0d1a0e588ace12244036cf993c2be7221f
Author: alexlehm <alexlehm@gmail.com>
Date:   Sat Jul 16 01:22:07 2016 +0200

    Squash commits into new branch:

    Implement http proxy requests, add unit test, add support for GET proxy requests
    to ConnectHttpProxy

    fix ProxyErrorTest which was missing setSsl(true)

    add tests for proxy errors for http, refactor the ProxyError tests a bit
    handle DNS errors in Proxy for GET properly

    add ProxyAuthentication to http proxy proxy requests

    change test support methods to private, remove one unused method, clarify comments a bit

    Signed-off-by: alexlehm <alexlehm@gmail.com>