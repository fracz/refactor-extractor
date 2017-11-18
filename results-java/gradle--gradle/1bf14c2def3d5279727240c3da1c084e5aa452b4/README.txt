commit 1bf14c2def3d5279727240c3da1c084e5aa452b4
Author: Szczepan Faber <szczepiq@gmail.com>
Date:   Mon Feb 11 18:31:12 2013 +0100

    REVIEW-1497 Steps to remove the ensureEvaluated() method. Some refactoring, more coverage.

    If this approach makes sense, we could look into protecting the other DefaultProjectAccessListener methods from attempting to configure project that is currently being configured.