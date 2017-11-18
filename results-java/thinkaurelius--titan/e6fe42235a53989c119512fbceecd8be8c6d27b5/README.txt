commit e6fe42235a53989c119512fbceecd8be8c6d27b5
Author: Dan LaRocque <dalaro@hopcount.org>
Date:   Thu Apr 17 08:50:42 2014 -0400

    Rewrite Timestamp refactoring

    Ran `git revert --no-commit af5de8a747d174642d582af19e8a8f8b50757dc3`
    and made substantial edits before committing, including retaining some
    changes that would have been reverted and rewriting some others.  This
    compiles and most tests pass (HBase has problems, there seem to be
    problems related to CString introduced in the last commit, I think a
    recent BDB bugfix on titan05 is missing, maybe other failures).

    This reverts commit af5de8a747d174642d582af19e8a8f8b50757dc3.