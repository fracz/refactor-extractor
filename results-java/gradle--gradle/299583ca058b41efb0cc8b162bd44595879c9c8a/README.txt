commit 299583ca058b41efb0cc8b162bd44595879c9c8a
Author: Sterling Greene <big-guy@users.noreply.github.com>
Date:   Fri Jul 14 11:49:36 2017 -0400

    Share outer builds worker lease with included build for parallel execution (#2464)

    * Use same parent lease for all included builds when executing tasks
    * Notify state changes when we clear a shared lease
    * Add documentation and refactor a bit
    * Rename some methods and make Javadoc sensible
    * Add test coverage for withSharedLease()
    * Assert that we see the expected amount of task concurrency
    * Add another test for composite build execution with max-workers=1
    * Fix Codenarc violations
    * Move concurrency assertion into build operations fixture