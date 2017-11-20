commit 524d5f2a3994b88d073f79c0d1ead0f23b0786ff
Author: Tinkoff DWH <tinkoff.dwh@gmail.com>
Date:   Sat May 20 22:38:11 2017 +0500

    [ZEPPELIN-2538] JDBC completer improvements for work with large meta

    ### What is this PR for?
    There are some problems if meta is large (few schemas, each schema contains 500+ tables etc.).
    Problems:

    1. loading is very long
    2. each update takes one connection if updates are long, the situation may arise that the entire pool will be busy
    3. no cache
    This PR solves these problems. Added cache and access by full path (schema.table, schema.table.column) + protection for release the connections

    ### What type of PR is it?
    Improvement

    ### What is the Jira issue?
    https://issues.apache.org/jira/browse/ZEPPELIN-2538

    ### Screenshots (if appropriate)
    ![peek 2017-05-15 15-03](https://cloud.githubusercontent.com/assets/25951039/26054252/4cef6980-3985-11e7-9719-e6138eb777f6.gif)

    ### Questions:
    * Does the licenses files need update? no
    * Is there breaking changes for older versions? no
    * Does this needs documentation? no

    Author: Tinkoff DWH <tinkoff.dwh@gmail.com>

    Closes #2343 from tinkoff-dwh/ZEPPELIN-2538 and squashes the following commits:

    0991c6ab [Tinkoff DWH] [ZEPPELIN-2538] small improvement
    e770d261 [Tinkoff DWH] [ZEPPELIN-2538] update description
    a5788743 [Tinkoff DWH] [ZEPPELIN-2538] protection long download
    f999488b [Tinkoff DWH] Merge remote-tracking branch 'upstream/master' into ZEPPELIN-2538
    f26ab5da [Tinkoff DWH] [ZEPPELIN-2538] fix tests
    d600fa16 [Tinkoff DWH] [ZEPPELIN-2538] rewrite sql completer to work with large data