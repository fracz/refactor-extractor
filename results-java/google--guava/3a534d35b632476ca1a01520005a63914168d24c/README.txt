commit 3a534d35b632476ca1a01520005a63914168d24c
Author: Chris Povirk <cpovirk@google.com>
Date:   Wed Aug 14 11:25:16 2013 -0400

    Actually fix tearDown handling that was made worse in CL 50062431.
    - The behavior before that CL was to ignore exceptions thrown by tearDown.
    - The behavior currently is to exceptions thrown by tearDown unless there was another exception thrown by the test itself, in which case we overwrite the test's exception with the tearDown's exception.
    - The new behavior is to respect the exception from tearDown unless an exception from the test itself exists to override it. This matches the JUnit behavior. Really.

    This time, I've added (internal-only) tests.
    I also improved some error logging in the tests while I was debugging.
    -------------
    Created by MOE: http://code.google.com/p/moe-java
    MOE_MIGRATED_REVID=50778046