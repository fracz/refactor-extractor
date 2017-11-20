commit 3f591c2327532679159019b9d7486805ef6d0768
Author: Jeff Zhang <zjffdu@apache.org>
Date:   Thu Sep 14 13:23:33 2017 +0800

    ZEPPELIN-2933. Code Refactoring of ZEPPELIN-1515 follow up

    ### What is this PR for?

    This is a refactoring PR of ZEPPELIN-1515. Because hadoop's FileSystem API not only works with hdfs, but also other hadoop compatible filesystem. So in this PR I rename it to `FileSystemNotebookRepo`

    ### What type of PR is it?
    [Refactoring]

    ### Todos
    * [ ] - Task

    ### What is the Jira issue?
    * https://issues.apache.org/jira/browse/ZEPPELIN-2933

    ### Questions:
    * Does the licenses files need update? No
    * Is there breaking changes for older versions? No
    * Does this needs documentation? No

    Author: Jeff Zhang <zjffdu@apache.org>

    Closes #2588 from zjffdu/ZEPPELIN-2933 and squashes the following commits:

    45d1e9b [Jeff Zhang] ZEPPELIN-2993. Code Refactoring of ZEPPELIN-1515 follow up