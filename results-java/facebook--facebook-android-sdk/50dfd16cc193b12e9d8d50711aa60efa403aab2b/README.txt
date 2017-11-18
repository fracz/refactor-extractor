commit 50dfd16cc193b12e9d8d50711aa60efa403aab2b
Author: Ming Li <mingfli@fb.com>
Date:   Thu Sep 27 10:53:34 2012 -0700

    Refactoring getIsOpened/getIsClosed to be isOpened/isClosed

    Summary: refactoring to remove "get" prefix.

    Test Plan: ran unit tests and spot tested apps.

    Reviewers: mmarucheck, clang, karthiks

    Reviewed By: mmarucheck

    CC: msdkexp@, platform-diffs@lists

    Differential Revision: https://phabricator.fb.com/D586105

    Task ID: 1441763