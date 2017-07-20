commit bdd898045d29d27838abb877943552427e404b6c
Author: Boone Gorges <boonebgorges@gmail.com>
Date:   Sat Mar 11 02:27:44 2017 +0000

    Don't run 'get_terms' filter when querying for terms within `get_term_by()`.

    Historically, it has been possible to call `get_term_by()` within
    a 'get_terms' filter callback. Since `get_term_by()` was refactored
    to use `get_terms()` internally [38677], callbacks of this nature
    have resulted in infinite loops.

    As a workaround, we introduce a 'suppress_filter' option to `get_terms()`,
    and use it when calling the function from within `get_term_by()`.

    Props ocean90.
    See #21760.
    Built from https://develop.svn.wordpress.org/trunk@40275


    git-svn-id: http://core.svn.wordpress.org/trunk@40192 1a063a9b-81f0-0310-95a4-ce76da25c4cd