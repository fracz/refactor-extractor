commit caf723570d8e52c52b1f6bd378f197c0af338c13
Author: markharwood <markharwood@gmail.com>
Date:   Fri May 15 17:03:16 2015 +0100

    Aggregations improvement: exclude clauses with a medium/large number of clauses fail.
    The underlying automaton-backed implementation throws an error if there are too many states.

    This fix changes to using an implementation based on Set lookups for lists of excluded terms.
    If the global-ordinals execution mode is in effect this implementation also addresses the slowness identified in issue 11181 which is caused by traversing the TermsEnum - instead the excluded terms’ global ordinals are looked up individually and unset the bits of acceptable terms. This is significantly faster.

    Closes #11176