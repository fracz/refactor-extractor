commit b41997850990c8ed99bb8d3d67c9a64371da7b84
Author: Igor Drozdov <igor_drozdov@epam.com>
Date:   Mon Feb 20 10:04:12 2017 +0300

    [MINOR] Use standard java API to interrupt thread

    ### What is this PR for?
    Use java `Thread.interrupt` method to stop job progress polling thread.

    Standard API is:
    * proper synchronized
    * able to interrupt `Thread.sleep` with any interval

    ### What type of PR is it?
    [Refactoring]

    ### Questions:
    * Does the licenses files need update? no
    * Is there breaking changes for older versions? no
    * Does this needs documentation? no

    Author: Igor Drozdov <igor_drozdov@epam.com>

    Closes #2039 from DrIgor/refactor-progress-poller and squashes the following commits:

    895787f [Igor Drozdov] Use standard java API to interrupt thread