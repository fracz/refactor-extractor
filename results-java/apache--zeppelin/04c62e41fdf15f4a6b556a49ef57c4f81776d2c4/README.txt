commit 04c62e41fdf15f4a6b556a49ef57c4f81776d2c4
Author: Jeff Zhang <zjffdu@apache.org>
Date:   Wed Dec 7 16:32:08 2016 +0800

    ZEPPELIN-1607. Refactor Livy Interpreter to adapt scope mode

    ### What is this PR for?
    ZEPPELIN-1606 will make scoped mode as the default interpreter mode of livy. This PR is to refactor livy interpreter to adapt the scoped mode. Besides, it also fix several bugs of livy interpreter.  like ZEPPELIN-1516. Besides that I also enable livy integration test in travis to make sure the refactoring correct.

    ### What type of PR is it?
    [Refactoring]

    ### Todos
    * Move session creation to open()
    * Other refactoring in livy interpreter, especially LivyHelper.java

    ### What is the Jira issue?
    * https://issues.apache.org/jira/browse/ZEPPELIN-1607

    ### How should this be tested?
    Travis is also enabled for livy integration test.

    ### Screenshots (if appropriate)

    ### Questions:
    * Does the licenses files need update? No
    * Is there breaking changes for older versions? No
    * Does this needs documentation? No

    Author: Jeff Zhang <zjffdu@apache.org>

    Closes #1612 from zjffdu/ZEPPELIN-1607 and squashes the following commits:

    980b796 [Jeff Zhang] run livy test separately
    2dd73e4 [Jeff Zhang] change version from 2.3 to 2.6 in .travis.yml
    6c9795f [Jeff Zhang] update test
    e99be8d [Jeff Zhang] update .travis.yml
    2ce1532 [Jeff Zhang] add licence header
    995ddf3 [Jeff Zhang] enable travis for livy integration test
    798de1b [Jeff Zhang] ZEPPELIN-1607. Refactor Livy Interpreter to adapt scope mode