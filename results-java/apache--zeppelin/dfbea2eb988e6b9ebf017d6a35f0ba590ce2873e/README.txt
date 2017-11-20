commit dfbea2eb988e6b9ebf017d6a35f0ba590ce2873e
Author: Khalid Huseynov <khalidhnv@gmail.com>
Date:   Sun Oct 23 21:52:23 2016 +0900

    [Zeppelin-1561] Improve sync for multiuser environment

    ### What is this PR for?
    apply multi-tenancy for storage sync mechanism

    ### What type of PR is it?
    Bug Fix | Improvement

    ### Todos
    * [x] - broadcast on sync
    * [x] - set permissions for pulled notes
    * [x] - add test

    ### What is the Jira issue?
     [ZEPPELIN-1561](https://issues.apache.org/jira/browse/ZEPPELIN-1561)

    ### How should this be tested?
    Outline the steps to test the PR here.

    ### Screenshots (if appropriate)
    green CI

    ### Questions:
    * Does the licenses files need update? no
    * Is there breaking changes for older versions? no
    * Does this needs documentation? no

    Author: Khalid Huseynov <khalidhnv@gmail.com>

    Closes #1537 from khalidhuseynov/improve/sync-multiuser and squashes the following commits:

    b3e6ed3 [Khalid Huseynov] add userAndRoles
    0f2ade7 [Khalid Huseynov] reformat style
    bd1a44a [Khalid Huseynov] address comment + test
    05afa2a [Khalid Huseynov] remove syncOnStart
    b104249 [Khalid Huseynov] add isAnonymous
    1a54cc0 [Khalid Huseynov] set perms for pulling notes - make them private
    585a675 [Khalid Huseynov] reload, sync and broadcast on login
    cd1c3fa [Khalid Huseynov] don't sync on start