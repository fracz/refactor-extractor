commit e822e4cbe87b09b901eb2363bc884a635a96a01e
Author: Sam Brannen <sam@sambrannen.com>
Date:   Mon Sep 5 13:39:21 2016 +0200

    Improve exception msg for inactive test ApplicationContext

    This commit improves the exception message thrown when a test's
    ApplicationContext is no longer active by explaining that the cause
    may be due to parallel test execution.

    Issue: SPR-5863