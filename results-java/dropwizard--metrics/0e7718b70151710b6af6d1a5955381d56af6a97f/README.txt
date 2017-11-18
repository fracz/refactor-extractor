commit 0e7718b70151710b6af6d1a5955381d56af6a97f
Author: Bogdan Storozhuk <storozhukBM@users.noreply.github.com>
Date:   Wed Jun 14 21:10:45 2017 +0300

    Issue #1138 Replacement of sliding time window. (#1139)

    * Drop-in replacement of sliding time window reservoir.
    With benchmark results.
    Code cleanup and refactoring required. Concurrency tests required.

    * chunks with two indexes

    * snapshot gathering optimisation

    * jcstress tests and few fixes

    * JCStress tests improvements and additional JMH benchmark for concurrent reads/writes

    * Load test plus results for new implementation.

    * PR preparation. Clean-up

    * Load test results for old implementation.

    * Test results clean up

    * Code clean-up.

    * Changes to ticks calculations in order to delay overflow and make once for 416 days from reservoir creation.

    * Change synchronization mechanism.