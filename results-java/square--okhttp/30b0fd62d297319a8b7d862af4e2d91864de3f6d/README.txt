commit 30b0fd62d297319a8b7d862af4e2d91864de3f6d
Author: Jesse Wilson <jwilson@squareup.com>
Date:   Fri Jun 6 00:29:19 2014 -0700

    Fix a regression in connection cleanup.

    We don't test that connections actually get closed, and this was
    broken by a refactoring that dropped close() on Connection.