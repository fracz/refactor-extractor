commit 4439a86cb8364742e977ea324bb174bcf7f51f06
Author: Christoph Büscher <christoph@elastic.co>
Date:   Wed May 25 15:31:59 2016 +0200

    Improve TimeUnitRounding for edge cases and DST transitions

    Our current testing for TimeUnitRoundings rounding() and nextRoundingValue()
    methods that are used especially for date histograms lacked proper randomization
    for time zones. We only did randomized tests for fixed offset time zones
    (e.g. +01:00, -05:00) but didn't account for real world time zones with
    DST transitions.

    Adding those tests revealed a couple of problems with our current rounding logic.
    In some cases, usually happening around those transitions, rounding a value down
    could land on a value that itself is not a proper rounded value. Also sometimes
    the nextRoundingValue would not line up properly with the rounded value of all
    dates in the next unit interval.

    This change improves the current rounding logic in TimeUnitRounding in two ways:
    it makes sure that calling round(date) always returns a date that when rounded
    again won't change (making round() idempotent) by handling special cases happening
    during dst transitions by performing a second rounding. It also changes the
    nextRoundingValue() method to itself rely on the round method to make sure we
    always return rounded values for the unit interval boundaries.

    Also adding tests for randomized TimeUnitRounding that assert important basic
    properties the rounding values should have. For better understanding and readability
    a few of the pathological edge cases are also added as a special test case.