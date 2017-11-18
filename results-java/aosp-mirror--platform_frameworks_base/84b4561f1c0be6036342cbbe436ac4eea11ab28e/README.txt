commit 84b4561f1c0be6036342cbbe436ac4eea11ab28e
Author: Muyuan Li <muyuanli@google.com>
Date:   Wed Jun 1 11:05:08 2016 -0700

    sysui: refactor for extensibility.

    Refactor the test whether touch point is inside
    QsContainer into a separate method.
    Make mScrollingEnabled protected.

    Bug: 28942125
    Change-Id: I21a620bbad12c25fedfa5a1139221bd4cb2e95fe
    (cherry picked from commit cc10182c291d3e559382ddd181b69c1e89e5ec8e)