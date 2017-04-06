commit 622d2c8e429b1f77d2b6f8e5bed6d5c9ef70fb32
Author: Lee Hinman <lee@writequit.org>
Date:   Thu Feb 5 11:51:18 2015 -0700

    [CORE] Refactor InternalEngine into AbstractEngine and classes

    InternalEngine contains a number of inner classes that it uses, however,
    this makes the class overly large and hard to extend. In order to be
    able to easily add other Engines (such as the ShadowEngine), these
    helping methods have been extracted into an AbstractEngine class. The
    classes that were previously in `InternalEngine` have been moved to
    separate classes, which will allow for better unit testing as well.

    None of the functionality of InternalEngine has been changed, this is
    only refactoring.

    Note that this is a change I originally made on my shadow-replica
    branch, however it is easier to review piecemeal so I extracted it into
    a separate PR.