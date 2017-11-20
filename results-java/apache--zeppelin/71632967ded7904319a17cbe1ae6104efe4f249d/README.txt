commit 71632967ded7904319a17cbe1ae6104efe4f249d
Author: Jeff Zhang <zjffdu@apache.org>
Date:   Fri Nov 25 16:33:39 2016 +0800

    ZEPPELIN-1707. Pass userName when creating interpreter through thrift

    ### What is this PR for?
    In ZEPPELIN-1607, I'd like refactor livy interpreter to scoped mode by default, this require username when open this interpreter. So I propose to pass username when creating interpreter through thrift.
    What I did in this PR.
    * update `RemoteInterpreterService.thrift` and regenerate the java thrift code.
    * update `genthrift.sh`, otherwise hashCode method won't be generated correctly.
    * This is one compilation issue (`PythonDockerInterpreterTest.java`) in the existing master branch, I also fix it here.

    ### What type of PR is it?
    [Improvement]

    ### Todos
    * [ ] - Task

    ### What is the Jira issue?
    * https://issues.apache.org/jira/browse/ZEPPELIN-1707

    ### Screenshots (if appropriate)

    ### Questions:
    * Does the licenses files need update? No
    * Is there breaking changes for older versions? No
    * Does this needs documentation? No

    Author: Jeff Zhang <zjffdu@apache.org>

    Closes #1679 from zjffdu/ZEPPELIN-1707 and squashes the following commits:

    763455f [Jeff Zhang] regenerate it using thrift 0.9.2
    a247552 [Jeff Zhang] ZEPPELIN-1707. Pass userName when creating interpreter through thrift