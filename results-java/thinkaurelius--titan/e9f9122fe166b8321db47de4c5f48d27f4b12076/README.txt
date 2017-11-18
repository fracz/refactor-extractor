commit e9f9122fe166b8321db47de4c5f48d27f4b12076
Author: Dan LaRocque <dalaro@hopcount.org>
Date:   Sat Jul 6 21:50:23 2013 -0700

    Refactoring ConsistentKeyLocker tests

    I've reduced what was previously a couple dozen lines of boilerplate
    EasyMock stubbing and setup into a set of five setup-helper methods
    that model conceptual primitives in EasyMock, like
    "recordSuccessfulLocalUnlock()", which internally records expectations
    with EasyMock that the locker will make a timestamp call followed by a
    call to the local lock mediator using that timestamp (order matters).

    This is definitely cleaner from a design point of view.  However, I'm
    not sure it's actually a practical improvement over copying
    boilerplate.  It seems a lot harder to figure out what EasyMock
    failures mean when the expectations involved in the failure are
    scattered across a bunch of little helper methods.  Still
    experimenting...