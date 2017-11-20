commit 152538b7efc22e2c4c449f9fec4e524d92f8653c
Author: Jeff Zhang <zjffdu@apache.org>
Date:   Mon Oct 16 11:28:23 2017 +0800

    ZEPPELIN-2990. Matplotlib sometimes fails in IPythonInterpreter

    ### What is this PR for?
    Trivial fix for matplotlib fail in `IPythonInterpreter`. Besides that, I also make some minor changes on zeppelin code to improve logging.

    ### What type of PR is it?
    [Bug Fix]

    ### Todos
    * [ ] - Task

    ### What is the Jira issue?
    * https://issues.apache.org/jira/browse/ZEPPELIN-2990

    ### How should this be tested?
    UT is added.

    ### Screenshots (if appropriate)

    ### Questions:
    * Does the licenses files need update? No
    * Is there breaking changes for older versions? No
    * Does this needs documentation? No

    Author: Jeff Zhang <zjffdu@apache.org>

    Closes #2622 from zjffdu/ZEPPELIN-2990 and squashes the following commits:

    55e6f88 [Jeff Zhang] fix unit test
    1e57afe [Jeff Zhang] ZEPPELIN-2990. Matplotlib sometimes fails in IPythonInterpreter