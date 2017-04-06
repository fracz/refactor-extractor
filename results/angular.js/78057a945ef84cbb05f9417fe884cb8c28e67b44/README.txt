commit 78057a945ef84cbb05f9417fe884cb8c28e67b44
Author: Igor Minar <igor@angularjs.org>
Date:   Mon Mar 17 15:48:09 2014 -0700

    fix(Scope): $watchCollection should call listener with oldValue

    Originally we destroyed the oldValue by incrementaly copying over portions of the newValue
    into the oldValue during dirty-checking, this resulted in oldValue to be equal to newValue
    by the time we called the watchCollection listener.

    The fix creates a copy of the newValue each time a change is detected and then uses that
    copy *the next time* a change is detected.

    To make `$watchCollection` behave the same way as `$watch`, during the first iteration
    the listener is called with newValue and oldValue being identical.

    Since many of the corner-cases are already covered by existing tests, I refactored the
    test logging to include oldValue and made the tests more readable.

    Closes #2621
    Closes #5661
    Closes #5688
    Closes #6736