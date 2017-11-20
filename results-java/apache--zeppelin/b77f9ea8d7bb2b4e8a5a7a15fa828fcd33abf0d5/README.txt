commit b77f9ea8d7bb2b4e8a5a7a15fa828fcd33abf0d5
Author: Khalid Huseynov <khalidhnv@gmail.com>
Date:   Tue Sep 27 12:56:29 2016 +0900

    [ZEPPELIN-1437, 1438] Multi-user note management - user aware reload broadcast

    ### What is this PR for?
    This PR addresses part of multi-user note management in Zeppelin. One of the tasks namely listing notes per user on Zeppelin start was addressed in #1330. However that PR didn't solve all problems, and reloading notes was incomplete as well as socket broadcast was not user aware [ZEPPELIN-1437](https://issues.apache.org/jira/browse/ZEPPELIN-1437), [ZEPPELIN-1438](https://issues.apache.org/jira/browse/ZEPPELIN-1438). This PR addresses those issue.

    ### What type of PR is it?
    Improvement

    ### Todos
    * [x] - list notes per user on reload
    * [x] - broadcast per user (multicast)
    * [x] - tests
    * [x] - use authorization module to filter notes on sync
    * [x] - broadcast on permissions change
    * [ ] - discussion and review

    ### What is the Jira issue?
    [Zeppelin-1437](https://issues.apache.org/jira/browse/ZEPPELIN-1437), [ZEPPELIN-1438](https://issues.apache.org/jira/browse/ZEPPELIN-1438)

    ### How should this be tested?
    1. Start Zeppelin
    2. Login as user1, and user2 on different windows
    3. Each user should be able to see their own note workbench
    4. If note changed to private (readers, writers not empty), that note should disappear from others note workbench.

    ### Screenshots (if appropriate)
    ![reload_broadcast](https://cloud.githubusercontent.com/assets/1642088/18679507/e4a0161c-7f9a-11e6-9d57-0930abf4b780.gif)

    ### Questions:
    * Does the licenses files need update? no
    * Is there breaking changes for older versions? no
    * Does this needs documentation? yes

    Author: Khalid Huseynov <khalidhnv@gmail.com>

    Closes #1392 from khalidhuseynov/feat/multi-user-notes and squashes the following commits:

    a2ce268 [Khalid Huseynov] broadcast note list on perm update - zeppelin-1438
    9cf1d88 [Khalid Huseynov] fix init not to initialize every time
    17eae84 [Khalid Huseynov] bugfix: add precondition for NP
    781207e [Khalid Huseynov] bugfix: reload only once
    537cc0e [Khalid Huseynov] apply filter from authorization in sync
    09e6723 [Khalid Huseynov] notebookAuthorization as singleton
    9427e62 [Khalid Huseynov] multicast fine grained note lists to users instead of broadcast
    6614e2b [Khalid Huseynov] improve tests
    1399407 [Khalid Huseynov] remove unused imports
    d9c3bc9 [Khalid Huseynov] filter reload using predicates
    92f37f5 [Khalid Huseynov] substitute old getAllNotes(subject) with new implementation
    b7f19c9 [Khalid Huseynov] separate getAllNotes() and getAllNotes(subject)
    17e2d4c [Khalid Huseynov] first draft