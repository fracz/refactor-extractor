commit 4179690250f18a2b0804eaffc1b47a7c66821cb8
Author: Michal Čihař <michal@cihar.com>
Date:   Tue Mar 21 11:18:03 2017 +0100

    Improved database search to allow search for exact phrase match

    Add option to search exact phrase as substring, what was currently
    missing.

    While doing that, also improve testsuite for generating the clauses.

    Fixes #12388

    Signed-off-by: Michal Čihař <michal@cihar.com>