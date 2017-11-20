commit f9830a7d64921c1f67aeb6ee179b574769e0a6f9
Author: Tinkoff DWH <tinkoff.dwh@gmail.com>
Date:   Wed Apr 5 12:32:44 2017 +0500

    [ZEPPELIN-2279] excluded comments from SQL

    ### What is this PR for?
    Exclusion comments (single-, multiline) from queries before execution. Comments don't need to execute  query and sometimes there are errors.

    ### What type of PR is it?
    Bug Fix | Improvement

    ### What is the Jira issue?
    https://issues.apache.org/jira/browse/ZEPPELIN-2279

    ### How should this be tested?
    ```
    /* ; */
    select 1;
    -- text select 1
    /* bla
    bla
    bla*/
    select 1; -- text
    ```

    ### Questions:
    * Does the licenses files need update? no
    * Is there breaking changes for older versions? no
    * Does this needs documentation? no

    Author: Tinkoff DWH <tinkoff.dwh@gmail.com>

    Closes #2158 from tinkoff-dwh/ZEPPELIN-2279 and squashes the following commits:

    3f7496e [Tinkoff DWH] [ZEPPELIN-2279] fix conditions, common format
    f48f7d6 [Tinkoff DWH] [ZEPPELIN-2279] improve test, revert  precode execution
    2cb94fa [Tinkoff DWH] Merge remote-tracking branch 'origin/master' into ZEPPELIN-2279
    6db3c46 [Tinkoff DWH] [ZEPPELIN-2279] excluded comments from SQL