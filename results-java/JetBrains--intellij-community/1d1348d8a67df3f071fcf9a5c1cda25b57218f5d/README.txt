commit 1d1348d8a67df3f071fcf9a5c1cda25b57218f5d
Author: Mikhail Golubev <mikhail.golubev@jetbrains.com>
Date:   Mon Nov 17 19:18:06 2014 +0300

    Remove dots before relative import source when replacing reference to moved package/module

    For now we strive to replace relative imports with absolute imports
    during "Move" refactoring. If we replace reference in specific
    import element like in "from ..pkg import moved" we substitute
    import statement altogether with "..pkg" part. However if reference was
    in source part of relative import, e.g. "from ..moved import smth",
    previously we'd only replaced corresponding reference expression
    ("moved") and left preceding dots untouched, and that was wrong.