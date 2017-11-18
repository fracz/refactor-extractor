commit 33923e4e83bedc9c21c07b0cccc422aae68269a5
Author: Szczepan Faber <szczepiq@gmail.com>
Date:   Mon Jun 1 21:12:19 2009 +0000

    Mean check-in (forgive me, the God of CI)
    Changed a bit stack trace filtering code. The problem with current stack trace filter was that it was not good for some edge cases with spies. Sometimes spy throws exception from real implementation. The idea is to make this exception looking like normal one - without mockito internals on the stack trace. Hence big refactoring and new classes & tests.

    --HG--
    rename : src/org/mockito/exceptions/base/ConditionalStackTraceFilter.java => src/org/mockito/internal/exceptions/base/ConditionalStackTraceFilter.java
    rename : src/org/mockito/exceptions/base/StackTraceFilter.java => src/org/mockito/internal/exceptions/base/StackTraceFilter.java
    rename : test/org/mockito/exceptions/base/ConditionalStackTraceFilterTest.java => test/org/mockito/internal/exceptions/base/ConditionalStackTraceFilterTest.java
    rename : test/org/mockito/exceptions/base/StackTraceFilterTest.java => test/org/mockito/internal/exceptions/base/StackTraceFilterTest.java
    extra : convert_revision : svn%3Aaa2aecf3-ea3e-0410-9d70-716747e7c967/trunk%401458