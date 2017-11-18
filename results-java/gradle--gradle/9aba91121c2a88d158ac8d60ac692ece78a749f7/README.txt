commit 9aba91121c2a88d158ac8d60ac692ece78a749f7
Author: Jendrik Johannes <jendrik@gradle.com>
Date:   Thu Oct 6 18:02:05 2016 +0200

    Deactivate log level propagation to java.util.Logging for now

    Currently, integration tests from in LoggingIntegrationTest and
    JavaUtilLoggingSystemIntegrationTest are flaky. Before we know if it
    only concerns tests (and how to fix those) I am restoring the original
    behaviour of always using the min level FINE. (Keeping the structural
    improvements to the logging code.)

    +review REVIEW-6255