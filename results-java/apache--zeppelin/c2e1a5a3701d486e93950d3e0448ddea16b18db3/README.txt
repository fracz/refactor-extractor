commit c2e1a5a3701d486e93950d3e0448ddea16b18db3
Author: Khalid Huseynov <khalidhnv@gmail.com>
Date:   Sun Dec 4 01:30:43 2016 +0900

    [ZEPPELIN-1776] substitute null check for the refactored Revision class

    ### What is this PR for?
    This is to fix minor bug introduced by #1697

    ### What type of PR is it?
    Bug Fix

    ### Todos
    * [x] - substitute null check

    ### What is the Jira issue?
    https://issues.apache.org/jira/browse/ZEPPELIN-1776

    ### Questions:
    * Does the licenses files need update? no
    * Is there breaking changes for older versions? no
    * Does this needs documentation? no

    Author: Khalid Huseynov <khalidhnv@gmail.com>

    Closes #1722 from khalidhuseynov/bugfix/revision-EMPTY and squashes the following commits:

    45c1df2 [Khalid Huseynov] refactor
    40e4e64 [Khalid Huseynov] add isEmpty()
    6650de8 [Khalid Huseynov] fix null -> EMPTY