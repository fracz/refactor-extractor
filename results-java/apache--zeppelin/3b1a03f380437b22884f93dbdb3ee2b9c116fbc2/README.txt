commit 3b1a03f380437b22884f93dbdb3ee2b9c116fbc2
Author: Jeff Zhang <zjffdu@apache.org>
Date:   Wed Nov 8 10:56:30 2017 +0800

    ZEPPELIN-3039. Interpreter logs are mixed together

    ### What is this PR for?

    This is a bug introduced by ZEPPELIN-2685. Wrong interpreter setting name is passed. This PR fix this issue and also made some code refactoring. After this PR, the log file name is

    ${ZEPPELIN_LOG_DIR}/zeppelin-interpreter-${INTERPRETER_SETTING_NAME}-${ZEPPELIN_IDENT_STRING}-${HOSTNAME}.log

    ### What type of PR is it?
    [Bug Fix]

    ### Todos
    * [ ] - Task

    ### What is the Jira issue?
    * https://issues.apache.org/jira/browse/ZEPPELIN-3039

    ### How should this be tested?
    Create 2 jdbc interpreters: hive & hive2,and launch them both. There would be 2 log files generated.
    * zeppelin-interpreter-hive-jzhang-HW12527.log
    * zeppelin-interpreter-hive2-jzhang-HW12527.log

    ### Screenshots (if appropriate)

    ### Questions:
    * Does the licenses files need update? No
    * Is there breaking changes for older versions? No
    * Does this needs documentation? No

    Author: Jeff Zhang <zjffdu@apache.org>

    Closes #2656 from zjffdu/ZEPPELIN-3039 and squashes the following commits:

    2745151 [Jeff Zhang] ZEPPELIN-3039. Interpreter logs are mixed together