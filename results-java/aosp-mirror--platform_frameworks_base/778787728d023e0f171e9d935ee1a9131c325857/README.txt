commit 778787728d023e0f171e9d935ee1a9131c325857
Author: Craig Mautner <cmautner@google.com>
Date:   Mon Mar 4 19:46:24 2013 -0800

    Fix activity to task bugs

    - Fix bug when task has been removed from mTaskHistory by
    adding it back in at the top.
    - Fix reparenting bug introduced by refactor of
    resetTaskIfNeededLocked.

    Change-Id: I93df2e62c6aed805fe888847dcf96a1fe0d7be26