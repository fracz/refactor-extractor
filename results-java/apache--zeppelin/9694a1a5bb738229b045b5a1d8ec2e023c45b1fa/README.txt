commit 9694a1a5bb738229b045b5a1d8ec2e023c45b1fa
Author: Jeff Zhang <zjffdu@apache.org>
Date:   Fri Nov 4 13:46:36 2016 +0800

    ZEPPELIN-1613. PerUser scoped mode doesn't work

    ### What is this PR for?
    For perUser scoped mode, difference users still use the same interpreter instance which is incorrect. This is due to they are using the same interpreter instance key.  This PR will fix this by using different interpreter instance key for different users. Besides, I also did some refactoring and add more log.

    ### What type of PR is it?
    [Bug Fix]

    ### Todos
    * [ ] - Task

    ### What is the Jira issue?
    * https://issues.apache.org/jira/browse/ZEPPELIN-1613

    ### How should this be tested?
    Tested manually.

    ### Screenshots (if appropriate)

    ### Questions:
    * Does the licenses files need update? No
    * Is there breaking changes for older versions? No
    * Does this needs documentation? No

    Author: Jeff Zhang <zjffdu@apache.org>

    Closes #1593 from zjffdu/ZEPPELIN-1613 and squashes the following commits:

    42bdc7b [Jeff Zhang] refactor interpreterInstanceKey to sessionKey
    20e07df [Jeff Zhang] fix perUser perNote combination
    5d751a6 [Jeff Zhang] ZEPPELIN-1613. PerUser scoped mode doesn't work