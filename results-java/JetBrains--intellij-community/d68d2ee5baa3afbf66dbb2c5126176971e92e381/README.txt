commit d68d2ee5baa3afbf66dbb2c5126176971e92e381
Author: Mikhail Golubev <mikhail.golubev@jetbrains.com>
Date:   Thu Feb 19 14:34:27 2015 +0300

    PY-12949 Don't rename directory instead of __init__.py if refactoring was invoked not from editor

    To preserve functionality that was added earlier with PY-3856, we rename
    directory of __init__.py only when refactoring was invoked from the
    editor (e.g. on the reference in import statement) and corresponding
    file otherwise (refactoring supposedly originated in project structure
    toolwindow).