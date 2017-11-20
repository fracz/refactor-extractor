commit 458556aad5a3cdfef6c5db931aa202a73bbf4cf7
Author: Prasad Wagle <pwagle@twitter.com>
Date:   Sat Apr 30 12:20:45 2016 -0700

    [ZEPPELIN-820] Reduce websocket communication by unicasting instead of broadcasting note list

    ### What is this PR for?
    Reducing websocket communication by unicasting instead of broadcasting notes list reduces probability of deadlock in jetty8 and also improves response time.

    ### What type of PR is it?
    Improvement

    ### Todos

    ### What is the Jira issue?
    [ZEPPELIN-820] (https://issues.apache.org/jira/browse/ZEPPELIN-820)

    ### How should this be tested?
    1. Open two browser windows.
    2. Check if note list shows up on home page.
    3. Check if creating, removing note in one window results in updates to notes list in other windows.

    ### Screenshots (if appropriate)

    ### Questions:

    Author: Prasad Wagle <pwagle@twitter.com>

    Closes #850 from prasadwagle/ZEPPELIN-820 and squashes the following commits:

    2c93621 [Prasad Wagle] Add broadcastNoteList() to updateNote, needed if there are changes to title
    f78781d [Prasad Wagle] Remove unnecessary calls to broadcastNoteList that uses broadcastAll
    f249ac1 [Prasad Wagle] [ZEPPELIN-820] Reduce websocket communication by unicasting instead of broadcasting note list