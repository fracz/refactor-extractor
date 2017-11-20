commit 6f434c5614e03d7c2ca9a6921c58c5d843b3dab0
Author: Prabhjyot Singh <prabhjyotsingh@gmail.com>
Date:   Sat Jun 25 19:53:49 2016 +0530

    [Zeppelin 1042] Extra space is present as part of username in search box

    ### What is this PR for?
    Sometimes extra space is present as part of username in search box while trying to setup Zeppelin permissions

    ### What type of PR is it?
    [Bug Fix]

    ### Todos
    * [x] - trim string and then add user
    * [x] - implement searching in Active Directory
    * [x] - improve order by search result
    * [x] - implement debounce to reduce server request

    ### What is the Jira issue?
    * [ZEPPELIN-1042](https://issues.apache.org/jira/browse/ZEPPELIN-1042)

    ### How should this be tested?
    Zeppelin is configured with LDAP authentication.
    Here is the scenario

    1. Login as 'user1' user and create a notebook ('Untitled Note 1')
    2. Try to change the permission of 'Untitled Note 1'. Start typing in owners box -> user1
    3. The search box appears with a saved name ' user1' (There is an extra space in front of user1')
    4. Then click on the search box item and save the permissions. The permissions that get saved have all got an extra space before the username 'user1' though it is not intended to have that space.
    5. Later while trying to change the permissions of notebook as 'user1' user, it will disallow because it recognizes ' user1' (user1 with extra space) as owner instead of plain 'user1'

    ### Questions:
    * Does the licenses files need update?
    * Is there breaking changes for older versions?
    * Does this needs documentation?

    Author: Prabhjyot Singh <prabhjyotsingh@gmail.com>

    Closes #1086 from prabhjyotsingh/ZEPPELIN-1042 and squashes the following commits:

    0de7a3d [Prabhjyot Singh] rename variable and CI fixes
    4d61f7d [Prabhjyot Singh] Merge remote-tracking branch 'origin/master' into ZEPPELIN-1042
    a3d5b5c [Prabhjyot Singh] trim and then add user
    57de67f [Prabhjyot Singh] implement debounce
    d5e5d96 [Prabhjyot Singh] update search preference
    179ea73 [Prabhjyot Singh] enable search for Active Directory