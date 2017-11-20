commit 7951e6c698aa2e009c461488c3231e23272e08bc
Author: Alexander Bezzubov <bzz@apache.org>
Date:   Mon Jun 27 20:43:09 2016 +0900

    ZEPPELIN-1063: fix flaky python interpreter test

    ### What is this PR for?
    fix flaky python interpreter test

    ### What type of PR is it?
    Bug Fix

    ### Todos
    * [x] cleanup test code
    * [x] fix flaky open port check

    ### What is the Jira issue?
    [ZEPPELIN-1063](https://issues.apache.org/jira/browse/ZEPPELIN-1063)

    ### How should this be tested?
    `mvn "-Dtest=org.apache.zeppelin.python.PythonInterpreterTest" test -pl python`

    ### Questions:
    * Does the licenses files need update? No
    * Is there breaking changes for older versions? No
    * Does this needs documentation? No

    Author: Alexander Bezzubov <bzz@apache.org>

    Closes #1094 from bzz/fix/python-tests/ZEPPELIN-1063 and squashes the following commits:

    b9de5a2 [Alexander Bezzubov] Python: refactoring open port checking
    09f3018 [Alexander Bezzubov] Python: refactoring - arrange imports
    46e42e8 [Alexander Bezzubov] Python: refactoring mock() structure and JavaDocs
    651d8e8 [Alexander Bezzubov] Python: refactoring loggers
    d7f8cdd [Alexander Bezzubov] Python: normalize newlines in tests