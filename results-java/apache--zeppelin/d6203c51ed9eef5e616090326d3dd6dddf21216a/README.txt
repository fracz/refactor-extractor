commit d6203c51ed9eef5e616090326d3dd6dddf21216a
Author: Jeff Zhang <zjffdu@apache.org>
Date:   Fri Sep 1 12:50:46 2017 +0800

    [ZEPPELIN-2627] Interpreter Component Refactoring

    What is this PR for?

    I didn't intended to make such large change at the beginning, but found many things are coupled together that I have to make such large change. Several suggestions for you how to review and read it.

    I move the interpreter package from zeppelin-zengine to zeppelin-interpreter, this is needed for this refactoring.
    The overall change is the same as I described in the design doc. I would suggest you to read the unit test first. These unit test is very readable and easy to understand what the code is doing now. `InterpreterFactoryTest`, `InterpreterGroupTest`, `InterpreterSettingTest`, `InterpreterSettingManagerTest`, `RemoteInterpreterTest`.
    Remove the referent counting logic. Now I will kill the interpreter process as long as all the sessions in the same interpreter group is closed. (I plan to add another kind of policy for the interpreter process lifecycle control, ZEPPELIN-2197)
    The RemoteFunction I introduced is for reducing code duplicates when we use RPC.
    The changes in `Job.java` and `RemoteScheduler` is for fixing the race issue bug. This bug cause the flaky test we see often in `ZeppelinSparkClusterTest.pySparkTest`
    What type of PR is it?

    [Bug Fix | Refactoring]

    Todos

     - Task
    What is the Jira issue?

    https://issues.apache.org/jira/browse/ZEPPELIN-2627
    How should this be tested?

    Unit test is added

    Screenshots (if appropriate)

    Questions:

    Does the licenses files need update? No
    Is there breaking changes for older versions? No
    Does this needs documentation? No

    Author: Jeff Zhang <zjffdu@apache.org>

    Closes #2554 from zjffdu/ZEPPELIN-2627-2 and squashes the following commits:

    fa0d435 [Jeff Zhang] minor update
    74bcb91 [Jeff Zhang] [ZEPPELIN-2627] Interpreter Component Refactoring