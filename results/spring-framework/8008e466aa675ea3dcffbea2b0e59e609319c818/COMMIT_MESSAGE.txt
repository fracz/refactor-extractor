commit 8008e466aa675ea3dcffbea2b0e59e609319c818
Author: Sam Brannen <sam@sambrannen.com>
Date:   Fri Aug 29 16:00:01 2014 +0200

    Refactor AssertThrows to support Throwable

    Prior to this commit, AssertThrows in the spring-test module only
    supported Exception; however, there are legitimate test cases where the
    subject under test (SUT) may potentially throw a subclass of Throwable.

    This commit refactors AssertThrows so that it supports exceptions of
    type Throwable instead of the limiting support for Exception.
    Furthermore, AssertThrows has been refactored to use generics (e.g.,
    Class<? extends Throwable> instead of merely Class).

    Issue: SPR-6362