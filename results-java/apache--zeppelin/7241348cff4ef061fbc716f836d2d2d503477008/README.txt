commit 7241348cff4ef061fbc716f836d2d2d503477008
Author: tinkoff-dwh <tinkoff.dwh@gmail.com>
Date:   Wed Nov 1 11:34:41 2017 +0300

    [ZEPPELIN-2813] revisions comparator

    ### What is this PR for?
    Sometimes need to see the difference between versions and to switch to another version and look for changes are not convenient (the page reloaded). This feature allows you to compare any two versions of the notebook.

    ### What type of PR is it?
    Feature

    ### What is the Jira issue?
    https://issues.apache.org/jira/browse/ZEPPELIN-2813

    ### How should this be tested?
    1 make some commits. Сchange the contents of paragraphs (delete, add, edit)
    2 open Revisions comparator
    3 compare revisions and check diff

    ### Screenshots (if appropriate)
    ![comparator](https://user-images.githubusercontent.com/25951039/28702781-cf1cedce-7378-11e7-9034-7036f4440bf3.gif)

    ### Questions:
    * Does the licenses files need update? yes (updated)
    * Is there breaking changes for older versions? no
    * Does this needs documentation? no

    Author: tinkoff-dwh <tinkoff.dwh@gmail.com>
    Author: Tinkoff DWH <tinkoff.dwh@gmail.com>

    Closes #2506 from tinkoff-dwh/ZEPPELIN-2813 and squashes the following commits:

    acc624f [tinkoff-dwh] Merge remote-tracking branch 'upstream/master' into ZEPPELIN-2813
    2fd89a8 [tinkoff-dwh] Merge remote-tracking branch 'origin/master' into ZEPPELIN-2813
    8b8afcc [tinkoff-dwh] Docs edit
    efa7ce2 [tinkoff-dwh] Merge branch 'master' into ZEPPELIN-2813
    f530524 [tinkoff-dwh] zep-2813 anim off
    0e866b2 [tinkoff-dwh] zep_2813 color change
    310760e [tinkoff-dwh] zep_2813 UI for REVISIONS COMPARATOR.
    3d4f86c [tinkoff-dwh] Merge branch 'master' into ZEPPELIN-2813_refactoring_2
    dc67f8f [tinkoff-dwh] [ZEPPELIN-2813] refactoring
    514b3f5 [tinkoff-dwh] small fixes, added documentation
    4ce5286 [tinkoff-dwh] Merge remote-tracking branch 'origin/master' into ZEPPELIN-2813
    b949814 [Tinkoff DWH] [ZEPPELIN-2813] license
    a192b95 [Tinkoff DWH] [ZEPPELIN-2813] revisions comparator for note