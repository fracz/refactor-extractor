commit ec8391a7fbc8ba67d1a04c9b93408230345fec36
Author: Brian Clozel <bclozel@pivotal.io>
Date:   Tue Dec 6 23:18:04 2016 +0100

    Fix Netty4ClientHttpRequestFactory POST/PUT requests

    This commit ensures that POST/PUT requests sent by the Netty client have
    a Content-Length header set.

    Integration tests have been refactored to use mockwebserver instead of
    Jetty and have been parameterized to run on all available supported
    clients.

    Issue: SPR-14860