commit 7a58d46103d8db4b81d4b2e2a18e9524a57cbd28
Author: Khalid Huseynov <khalidhnv@nflabs.com>
Date:   Tue Feb 9 02:38:46 2016 +0900

    Zeppelin-540: Notebook versioning control, commit from frontend notebook menu

    ### What is this PR for?
    This PR improves the front-end experience to use git versioning on notebooks.
    Any community feedback is welcome.

    ### What type of PR is it?
    Improvement

    ### Todos
    * [x] - front-end and back-end changes
    * [x]  - add `checkpoint` interface
    * [x] - ~~error propagation to front-end~~ in a separate PR
    * [x] - tests
    * [x] - UI feedback?

    ### Is there a relevant Jira issue?
    [ZEPPELIN-540](https://issues.apache.org/jira/browse/ZEPPELIN-540)

    ### How should this be tested?
    1. Uncomment `GitNotebookRepo` as a storage in `/conf/zeppelin-site.xml`
    2. Start Zeppelin
    3. Make changes to some notebook
    4. Use `git` menu to commit
    5. To check whether successfully committed, do `git log` in `/notebooks` folder

    ### Screenshots (if appropriate)
    ![zeppelin commit notebook](https://cloud.githubusercontent.com/assets/1642088/12534987/3a0e72c2-c2b5-11e5-827e-33ba36826637.gif)

    ### Questions:
    * Does the licenses files need update? No
    * Is there breaking changes for older versions? No
    * Does this needs documentation? No

    Author: Khalid Huseynov <khalidhnv@nflabs.com>

    Closes #577 from khalidhuseynov/feat/git-commit-frontend-access and squashes the following commits:

    4f04af0 [Khalid Huseynov] remove initial commit at start
    64111e0 [Khalid Huseynov] Merge branch 'master' into feat/commit-frontend-access
    5f10b46 [Khalid Huseynov] backend gitCommit -> commit
    ef3a8fb [Khalid Huseynov] Merge branch 'master' into feat/commit-frontend-access
    8b5766f [Khalid Huseynov] change icon  git -> file-code-o
    74db747 [Khalid Huseynov] remove git from wording
    a03a953 [Khalid Huseynov] add repoSync checkpoint test for 1 storage
    33f6e0f [Khalid Huseynov] add gitNotebookRepo checkpoint test
    b0fa219 [Khalid Huseynov] only commit message
    339c9fe [Khalid Huseynov] Merge  'master' into feat/git-commit-frontend-access
    39c2873 [Khalid Huseynov] use sinqle quotes
    b8fc035 [Khalid Huseynov] empty input field after commit message is hit
    c9192cb [Khalid Huseynov] temp fix for padding issue
    b69f2ff [Khalid Huseynov] change div to button
    39c8e00 [Khalid Huseynov] fix formatting for checkstyle
    32e403c [Khalid Huseynov] propagate changes to frontend
    bdeb0ed [Khalid Huseynov] add checkpoint interface and propagate to backend
    de7bdd6 [Khalid Huseynov] add checkpoint interface
    a1894c2 [Khalid Huseynov] backend handling of git notebook commit
    8069ccd [Khalid Huseynov] add frontend handler via websocket
    2ebe628 [Khalid Huseynov] add button and git menu