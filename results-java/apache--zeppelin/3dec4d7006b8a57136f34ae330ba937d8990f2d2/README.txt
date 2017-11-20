commit 3dec4d7006b8a57136f34ae330ba937d8990f2d2
Author: Jeff Zhang <zjffdu@apache.org>
Date:   Mon Aug 8 08:40:46 2016 +0800

    ZEPPELIN-1287. No need to call print to display output in PythonInterpreter

    ### What is this PR for?
    It is not necessary to call print to display output in PythonInterpreter. 2 main changes:
    * the root cause is the displayhook in bootstrap.py
    * also did some code refactoring on PythonInterpreter

    ### What type of PR is it?
    [Bug Fix]

    ### Todos
    * [ ] - Task

    ### What is the Jira issue?
    * https://issues.apache.org/jira/browse/ZEPPELIN-1287

    ### How should this be tested?
    Verify it manually

    ### Screenshots (if appropriate)
    ![2016-08-04_1404](https://cloud.githubusercontent.com/assets/164491/17392006/090279d2-5a4d-11e6-840b-4cddb595a42e.png)

    ### Questions:
    * Does the licenses files need update? No
    * Is there breaking changes for older versions? No
    * Does this needs documentation? No

    Author: Jeff Zhang <zjffdu@apache.org>

    Closes #1278 from zjffdu/ZEPPELIN-1287 and squashes the following commits:

    b48b56f [Jeff Zhang] fix unit test fail
    3e9f169 [Jeff Zhang] address comments
    0eade71 [Jeff Zhang] ZEPPELIN-1287. No need to call print to display output in PythonInterpreter