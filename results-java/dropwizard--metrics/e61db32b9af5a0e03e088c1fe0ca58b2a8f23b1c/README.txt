commit e61db32b9af5a0e03e088c1fe0ca58b2a8f23b1c
Author: Artem Prigoda <arteamon@gmail.com>
Date:   Fri Jun 2 10:49:51 2017 +0200

    Fix metrics httpasyncclient timing master (#885)

    * httpasyncclient: Fix instrumentation of the InstrumentedNHttpClientBuilder

    Previously the Timer only measured the creation of the Future but not
    the execution of the request.
    In order to time the actual request execution a custom `FutureCallback` is used.

    Add a separate TimerTest that ensures that the Timer is stopped
    correctly (i.e. after the response is received and not after the Future
    is created).

    This change might have a significant impact on current consumers as
    the scope of what is measured changes.

    * httpasyncclient: Improve tests to use a local test server, improve timer test case

    Instead of making a request to http://example.com, the tests start
    a local test server that will be used for testing. This not only allows
    test to use custom response (if necessary) but should improve the test
    as they don't rely on an external resource anymore.

    * httpasyncclient: Don't throw AssertionError, throw unchecked exception instead

    (cherry picked from commit e2a3cc67599ccb6f5616337cf70a8c756f9e687e)

    # Conflicts:
    #       metrics-httpasyncclient/src/main/java/com/codahale/metrics/httpasyncclient/InstrumentedNHttpClientBuilder.java
    #       metrics-httpasyncclient/src/test/java/com/codahale/metrics/httpasyncclient/InstrumentedHttpClientsTest.java