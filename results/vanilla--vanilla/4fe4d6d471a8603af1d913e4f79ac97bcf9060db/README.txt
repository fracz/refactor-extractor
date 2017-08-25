commit 4fe4d6d471a8603af1d913e4f79ac97bcf9060db
Author: Todd Burry <todd@vanillaforums.com>
Date:   Tue Dec 13 15:58:24 2016 -0500

    Simplify the request parsing logic a bit

    There are two main changes here:

    1. Check for X_REWRITE first since that is the situation a properly configured installation will have. If X_REWRITE is found then don’t check $_GET for a path argument.

    2. Remove some redundant logic when parsing the $_GET that was probably a result of previous refactoring.