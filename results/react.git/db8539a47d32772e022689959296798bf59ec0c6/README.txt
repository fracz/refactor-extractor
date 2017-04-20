commit db8539a47d32772e022689959296798bf59ec0c6
Author: Andrew Clark <acdlite@fb.com>
Date:   Fri Dec 2 15:27:00 2016 -0800

    Consolidate work loops

    This makes it easier to track when we enter and exit a batch of work.

    Further steps needed in this refactor:
    - Get rid of tryComponentDidMount, tryComponentDidUpdate, etc. in
    favor of the try-catch blocks that wrap each pass of the commit phase.
    - Need to be able to switch between performing work that is possibly
    failed (slower because it requires an extra check on each iteration) and
    work that we know for sure has no errors.