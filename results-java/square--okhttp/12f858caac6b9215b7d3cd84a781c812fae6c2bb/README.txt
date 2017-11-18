commit 12f858caac6b9215b7d3cd84a781c812fae6c2bb
Author: Jesse Wilson <jwilson@squareup.com>
Date:   Sun Nov 2 09:13:50 2014 -0500

    Attempt to improve MockWebServer reliability.

    Use the same InetAddress when binding to a port as when offering the
    MockWebServer's address.

    Wait for the executors to all shutdown before returning from shutdown().