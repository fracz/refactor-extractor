commit 9812e26bced75033b5795ee0fb8d8c08df9c83b5
Author: Jeff Zhang <zjffdu@apache.org>
Date:   Fri Sep 22 15:00:00 2017 +0800

    ZEPPELIN-2685. Improvement on Interpreter class

    ### What is this PR for?
    Follow up of #2577. Main changes on Interpreter
    * Add throw `InterpreterException` which is checked exception for the abstract methods of `Interpreter`, this would enforce the interpreter implementation to throw `InterpreterException`.
    * field name refactoring.

         * `property` -> `properties`
         * `getProperty()` --> `getProperties()`
    * Introduce launcher layer for interpreter launching. Currently we only use shell script to launch interpreter, but it could be any other service or component to launch interpreter, such as livy server , other 3rd party tools or even we may create a separate module for interpreter launcher

         * abstract cass `InterpreterLauncher`
         * For now, only 2 implementation: `ShellScriptLauncher` & `SparkInterpreterLauncher`. We could add method in class `Interpreter` to allow interpreter to specify its own launcher class, but it could be future work.

    ### What type of PR is it?
    [Improvement | Refactoring]

    ### Todos
    * [ ] - Task

    ### What is the Jira issue?
    * https://issues.apache.org/jira/browse/ZEPPELIN-2685

    ### How should this be tested?
    Unit test is covered. `ShellScriptLauncherTest` & `SparkInterpreterLauncherTest`

    ### Screenshots (if appropriate)

    ### Questions:
    * Does the licenses files need update? No
    * Is there breaking changes for older versions? No
    * Does this needs documentation? No

    Author: Jeff Zhang <zjffdu@apache.org>

    Closes #2592 from zjffdu/ZEPPELIN-2685 and squashes the following commits:

    17dc2f1 [Jeff Zhang] address comments
    e545cc3 [Jeff Zhang] ZEPPELIN-2685. Improvement on Interpreter class