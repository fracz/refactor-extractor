commit e94d5c0fb66ee0954a74450002c5b81b8a7331a7
Author: Khalid Huseynov <khalidhnv@gmail.com>
Date:   Thu Jan 12 15:12:29 2017 -0800

    [ZEPPELIN-1961] Improve stability of sync when get fails

    ### What is this PR for?
    This is to improve the stability of sync mechanism when `get` from some backend storage fails (e.g. corrupt file, network issues).

    ### What type of PR is it?
    Bug Fix |  Hot Fix

    ### Todos
    * [x] - handle exception

    ### What is the Jira issue?
    [ZEPPELIN-1961](https://issues.apache.org/jira/browse/ZEPPELIN-1961)

    ### How should this be tested?
    CI green

    ### Screenshots (if appropriate)

    ### Questions:
    * Does the licenses files need update? no
    * Is there breaking changes for older versions? no
    * Does this needs documentation? no

    Author: Khalid Huseynov <khalidhnv@gmail.com>

    Closes #1895 from khalidhuseynov/fix-stability/sync-fail and squashes the following commits:

    aa1e199 [Khalid Huseynov] catch failed get command