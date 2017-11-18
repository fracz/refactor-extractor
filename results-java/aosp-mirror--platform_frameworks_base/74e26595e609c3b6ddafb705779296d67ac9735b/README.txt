commit 74e26595e609c3b6ddafb705779296d67ac9735b
Author: Wale Ogunwale <ogunwale@google.com>
Date:   Fri Feb 5 11:48:37 2016 -0800

    Further improvement to determining visiblility of tasks in home stack.

    673cbd2b6932b39d6804cda2969b7f059c1ce748 introduced logic to make
    non-top activities in the home stack invisible under certain
    conditions. However, this caused problems with the set-up wizard which
    uses the home stack to launch all its activties. Changed the logic to
    determine if the next task in the home stack should be visible behind
    vs. the next activity in the same task. So, activities in the same task
    in the home stack can be visible at the same time, but not activities
    in different tasks.

    Bug: 26922407
    Bug: 26571156
    Change-Id: Ied20d45cd27a1adcc105703d8ca21861d1856700