commit c2c6af946ce4609144049a5d89480702878d52f8
Author: Tim Hunt <T.J.Hunt@open.ac.uk>
Date:   Fri Apr 26 17:51:23 2013 +0100

    MDL-22390 autolink filter: handle URLs in brackets better.

    This breaks some legitimate URLs like
    http://en.wikipedia.org/wiki/Slash_(punctuation).
    This is a necessary trade-off. Many other web systems do not handle that
    case correctly either. The work-around it so escape the ) as %29.

    This commit also improves the way the unit tests for this work.

    It also fixes a couple of other tricky cases that were spotted in
    the forums while this was being discussed. See the new test cases.