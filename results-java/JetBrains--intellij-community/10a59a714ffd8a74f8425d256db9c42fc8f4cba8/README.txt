commit 10a59a714ffd8a74f8425d256db9c42fc8f4cba8
Author: Dmitry Jemerov <yole@jetbrains.com>
Date:   Tue Aug 30 19:37:01 2011 +0200

    to ensure correct undo, perform the refactoring in moveOffsetAfter() and not in finish() (PY-4416)