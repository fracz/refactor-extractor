commit 7ca5fba05ff0ef4dcf076303e1cba19c4b771d94
Author: Chris Beams <cbeams@vmware.com>
Date:   Fri Feb 24 14:29:28 2012 +0100

    Avoid infinite loop in AbstractResource#contentLength

    Due to changes made in commit 2fa87a71 for SPR-9118,
    AbstractResource#contentLength would fall into an infinite loop unless
    the method were overridden by a subclass (which it is in the majority of
    use cases).

    This commit:

     - fixes the infinite recursion by refactoring to a while loop

     - asserts that the value returned from #getInputStream is not null in
       order to avoid NullPointerException

     - tests both of the above

     - adds Javadoc to the Resource interface to clearly document that the
       contract for any implementation is that #getInputStream must not
       return null

    Issue: SPR-9161