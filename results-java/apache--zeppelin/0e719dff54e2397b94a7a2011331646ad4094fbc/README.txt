commit 0e719dff54e2397b94a7a2011331646ad4094fbc
Author: Vinay Shukla <vshukla@HW12335.local>
Date:   Tue May 3 15:39:25 2016 -0700

    Fixing Zeppelin-838: Minor improvement to error message

    ### What is this PR for?
    This JIRA improves the error message displayed when the current user opens a notebook to which the user is not allowed access to.

    ### What type of PR is it?
    [Minor Improvement ]

    ### Todos
    * None

    ### What is the Jira issue?
    https://issues.apache.org/jira/browse/ZEPPELIN-838

    ### How should this be tested?
    Get the change in this pull request. Build Zeppelin, configure shiro authentication by commenting out the 1st line below and modifying the corresponding line for 2nd line.
    #/** = anon
    /** = authcBasic
    Log in as user1, configure a notebook to only allow access to only user1, now access that same notebook as  user2 and note the new error message.

    ### Screenshots (if appropriate)

    [Screenshot:-Before](https://issues.apache.org/jira/secure/attachment/12802057/Screen%20Shot%202016-05-03%20at%203.16.58%20PM.png)
    [Screenshot:-After](https://issues.apache.org/jira/secure/attachment/12802065/Screen%20Shot%202016-05-03%20at%203.17.36%20PM.png)

    ### Questions:
    * Does the licenses files need update? --No
    * Is there breaking changes for older versions? --No
    * Does this needs documentation? -- No

    Author: Vinay Shukla <vshukla@HW12335.local>

    Closes #872 from vinayshukla/zeppelin-838 and squashes the following commits:

    ce4b3bd [Vinay Shukla] Fixing Zeppelin-838: Minor improvement to error message