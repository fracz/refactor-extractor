commit 1c4d07c96f060fd5fbd9e27a799071c178642985
Author: Lee Hinman <lee@writequit.org>
Date:   Fri Oct 17 13:03:36 2014 +0200

    Allow setting individual breakers to "noop" breakers

    This adds a NoopCircuitBreaker, and then adds the settings
    `indices.breaker.fielddata.type` and `indices.breaker.request.type`,
    which can be set to "noop" in order to use a breaker that will never
    break, and incurs no overhead during computation.

    This also refactors the tests for the CircuitBreakerService to use
    @Before and @After functions as well as adding settings in
    ElasticsearchIntegrationTest to occasionally use NOOP breakers for all
    tests.