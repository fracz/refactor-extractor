commit b54c1795c70825977700f075c8a6d0ee2a17bf4b
Author: jrfnl <jrfnl@users.noreply.github.com>
Date:   Mon Aug 7 03:09:15 2017 +0200

    :sparkles: Add new `CodeAnalysis.AssignmentInCondition` sniff to `Extra`

    Most of the time variable assignments being made in conditions are unintentional and are in actual fact typos which were intended as a comparison.
    In those cases when it isn't a typo, this is a typical code smell which is a prime candidate for refactoring.

    This sniff will warn when any such a variable assignment is found in a condition.

    Note: the sniff does currently **not** detect variable assignments in the conditional part of ternaries.

    Includes extensive unit tests.

    Inspired by [this twitter thread](https://twitter.com/jrf_nl/status/893891839903838209), or rather: the responses I received to my tweet.

    Note: the fact that WP uses Yoda conditions does **not** invalidate the need for this sniff.
    The Yoda conditions sniff only checks comparison operators, not assignment operators.

    I've run this sniff over WP Core to:
    a) test the sniff and
    b) see what it would turn up

    And found 1004 cases in 847 files and visual verification of a sample of theses results showed no false positives.

    AFAICS the cases found are intentional assignments within conditions, however, as said above, that is an indicator that the code in question is due for some refactoring as this is definitely a code smell.

    This sniff is a duplicate of the same which I've pulled upstream: squizlabs/PHP_CodeSniffer/pull/ 1594
    The upstream sniff has been merged and is due to be included in PHPCS 3.1.0. Once the minimum PHPCS requirement for WPCS has gone up beyond that version (i.e. after PHPCS 2.x support has been dropped), we can safely remove the WPCS version of the sniff.