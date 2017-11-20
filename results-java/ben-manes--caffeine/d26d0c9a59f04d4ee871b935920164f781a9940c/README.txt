commit d26d0c9a59f04d4ee871b935920164f781a9940c
Author: Ben Manes <ben.manes@gmail.com>
Date:   Thu Aug 11 02:09:31 2016 -0700

    Fix reference cache triggering maintenance after a write (fixed #111)

    This oversight caused write methods to not try to clean-up collected
    entries for caching using only the weak/soft reference configurations.
    The maintenance would be triggered after enough reads or if manually
    calling cleanUp().

    Most of the changes are improvements to the unit tests, as test flaws
    allowed this bug to be introduced. Primarily the cause was due to not
    being strict enough when evaluating the removal notifications. The tests
    now highlight a slight difference in Guava & Caffeine regarding if
    replacing a value with the same instance triggers a notification. Other
    small mistakes in the reference cache tests were uncovered and fixed.