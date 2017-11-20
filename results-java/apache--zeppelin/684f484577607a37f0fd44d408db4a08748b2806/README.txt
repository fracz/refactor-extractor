commit 684f484577607a37f0fd44d408db4a08748b2806
Author: Jeff Zhang <zjffdu@apache.org>
Date:   Thu Jan 26 23:29:47 2017 +0800

    ZEPPELIN-2015 Improve parsing logic of livy sql output

    ### What is this PR for?
    This PR is trying to resolve the table display issue related with #1942. But when I do this PR, I find other 2 issues in livy interpreter.
    * livy integration is never run due to refactoring of travis script in #1786.
    * pyspark integration would fail

    ### What type of PR is it?
    [Bug Fix | Improvement]

    ### Todos
    * [ ] - Task

    ### What is the Jira issue?
    https://issues.apache.org/jira/browse/ZEPPELIN-2015

    ### Screenshots (if appropriate)

    ### Questions:
    * Does the licenses files need update? No
    * Is there breaking changes for older versions? No
    * Does this needs documentation? No

    Author: Jeff Zhang <zjffdu@apache.org>
    Author: Chin Tzulin <jp20316@w022341412910m.local>

    Closes #1950 from zjffdu/ZEPPELIN-2015 and squashes the following commits:

    3ddb587 [Jeff Zhang] update travis
    ded4118 [Jeff Zhang] Revert "[ZEPPELIN-1982] When using the 'Select * ...' statement doesn't show the response In %sql interpreter"
    16a18de [Jeff Zhang] Fix appId and webui extraction for pyspark
    3f86bff [Jeff Zhang] ZEPPELIN-2015. Improve parsing logic of livy sql output
    043b03b [Chin Tzulin] [ZEPPELIN-1982] When using the 'Select * ...' statement doesn't show the response In %sql interpreter