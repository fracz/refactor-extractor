commit 223d225e0119d9e3013206a211df6b70a71b6a66
Author: WeichenXu <WeichenXu123@outlook.com>
Date:   Sun Aug 28 00:44:26 2016 -0700

    [ZEPPELIN-1383][ Interpreters][r-interpreter] remove SparkInterpreter.getSystemDefault and update relative code

    ### What is this PR for?
    clean some redundant code:
    remove `SparkInterpreter.getSystemDefault` methods,
    and replace it with `InterpreterProperty.getValue`

    ### What type of PR is it?
    Improvement

    ### Todos
    N/A

    ### What is the Jira issue?
    https://issues.apache.org/jira/browse/ZEPPELIN-1383
    remove SparkInterpreter.getSystemDefault and update relative code

    ### How should this be tested?
    Existing tests.

    ### Screenshots (if appropriate)

    ### Questions:
    * Does the licenses files need update? No
    * Is there breaking changes for older versions? No
    * Does this needs documentation? No

    Author: WeichenXu <WeichenXu123@outlook.com>

    Closes #1372 from WeichenXu123/remove_spark_interpreter_getSystemDefault and squashes the following commits:

    204a34c [WeichenXu] improve code stype.
    841b757 [WeichenXu] update.