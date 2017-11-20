commit 90cc2b3d1b6902cc56c7231a6802a3baac3ee0d7
Author: karuppayya <karuppayya1990@gmail.com>
Date:   Sun Jan 24 21:24:16 2016 +0530

    Shell interpreter improvements

    Creating new PR with the changes from
    https://github.com/apache/incubator-zeppelin/pull/615
    Please check the above PR for prior discussions.

    ### What is this PR for?
    *Provide ability to to run  shell commands in parallel
    *Provide ability to cancel shell command
    *Propagate the error from shell commands to UI

    ### What type of PR is it?
    Improvement

    ### Todos
    NA

    ### Is there a relevant Jira issue?
    No

    ### How should this be tested?
    *To check parallelism, run more than 10 shell commands concurrently.
    *To verify whether error is propagate to UI, execute a shell command which will error out(simplest being cd of a non existent directory )
    *To verify the cancel functionality, try cancelling a shell command that is running.

    ### Screenshots (if appropriate)
    NA

    ### Questions:
    * Does the licenses files need update? No
    * Is there breaking changes for older versions? No
    * Does this needs documentation? No

    Author: karuppayya <karuppayya1990@gmail.com>
    Author: Karup <karuppayya@outlook.com>

    Closes #666 from Karuppayya/shell_imp and squashes the following commits:

    6293781 [karuppayya] Fix test failure, fixes based on discussion
    431cc79 [karuppayya] Shell interpreter improvements
    825f696 [karuppayya] merge master
    4fd2113 [Karup] Send exitvalue of  shell command in interpreter result
    d259c48 [karuppayya] Fix typo, log exit value of a succesful shell commnad
    351888d [karuppayya] Increase thread pool size
    8cd6fd4 [karuppayya] Add log messages
    9eb3eca [karuppayya] Fix command timeout period
    87364b1 [karuppayya] Remove unnecessary changes
    fcdc494 [karuppayya] Fix indentation
    30078ac [karuppayya] fix
    540bfa8 [Karup] Merge branch 'shell1' of github.com:Karuppayya/incubator-zeppelin into shell1
    7d938bd [Karup] fix
    b0a97a1 [Karup] fix