commit f6b58ee5a06d32af37903cc768e106079d267b2d
Author: tinkoff-dwh <tinkoff.dwh@gmail.com>
Date:   Mon Jul 31 15:56:34 2017 +0500

    [HOTFIX] JDBC completions. fix cursor position

    ### What is this PR for?
    If text of paragraph contains interpreter name then parser works incorrect (cursor position  incorrect).

    ### What type of PR is it?
    Bug Fix

    ### Screenshots (if appropriate)
    before
    ![before](https://user-images.githubusercontent.com/25951039/28326867-55195f50-6bfb-11e7-8f93-c381658d598a.png)
    after
    ![after](https://user-images.githubusercontent.com/25951039/28326904-6f053fa6-6bfb-11e7-8586-00da4fa95b21.png)

    ### Questions:
    * Does the licenses files need update? no
    * Is there breaking changes for older versions? no
    * Does this needs documentation? no

    Author: tinkoff-dwh <tinkoff.dwh@gmail.com>
    Author: Tinkoff DWH <tinkoff.dwh@gmail.com>

    Closes #2495 from tinkoff-dwh/completion_hotfix and squashes the following commits:

    bac1ac2 [tinkoff-dwh] Merge remote-tracking branch 'upstream/master' into completion_hotfix
    80c8e2a [Tinkoff DWH] small refactoring
    5e9aa73 [tinkoff-dwh] add more data to dataset of test
    9f3dbc7 [tinkoff-dwh] added test
    43f8ffd [tinkoff-dwh] another solution to fix cursor position + small fix to return table names
    4e6347a [Tinkoff DWH] fix cursor position for completions