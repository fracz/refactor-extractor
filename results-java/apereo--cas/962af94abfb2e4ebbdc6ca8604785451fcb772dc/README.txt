commit 962af94abfb2e4ebbdc6ca8604785451fcb772dc
Author: David Rodriguez Gonzalez <davidrg131092@gmail.com>
Date:   Tue Nov 8 15:49:06 2016 +0100

    Rest improvements (#2113)

    * Use DeferredResult in TGT creating endpoint

    * Fix tests

    * Create Jackson Writer only once

    * Use StringBuilder over Formatter as it's almost 10x faster (355707.814 ops/s vs 2652050.343 ops/s)

    * Add TODOs for tasks

    * Extract stirng constants

    * Do all the concatenations we can on start up. Add an initial capacity to the StringBuilder

    * Iterate over values as keys are not used

    * Log error in one line

    * Remove TODO

    * Cleaning checkstyle

    * Do not use DeferredResult as we need to test it deeply

    * Fix tests for non DeferredResult implementation