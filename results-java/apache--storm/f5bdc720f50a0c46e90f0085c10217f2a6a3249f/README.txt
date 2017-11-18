commit f5bdc720f50a0c46e90f0085c10217f2a6a3249f
Author: Michael G. Noll <michael@michael-noll.com>
Date:   Fri Dec 7 12:31:03 2012 +0100

    Completely refactor Rolling Count example

    The new implementation separates the various concerns formerly mixed up in the
    example code.  Also, we are now using tick tuples (introduced in Storm 0.8)
    instead of spawning manual threads for carrying out periodical tasks.

    Lastly, we add 192 unit tests for the new implementation that brings the test
    coverage for the Rolling Count from 0% to almost 100%.

    Note: Adding those unit tests required changes to the build (m2-pom.xml),
    notably new test dependencies and moving the existing Java code from src/jvm/*
    to src/jvm/main/*.  The latter was required so that the test runner triggered
    by Maven can tell code (src/jvm/main) and tests (src/jvm/test) apart.