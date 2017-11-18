commit 471ab5b5c32198756ccfb41bf3a4f6f74272f3e7
Author: Mikhail Golubev <mikhail.golubev@jetbrains.com>
Date:   Mon Mar 6 15:56:14 2017 +0300

    PY-22422 Consider new code style settings in actions that might create/update "from" imports

    In particular, in "Import ..." quickfix, smart completion for class
    names and in a few refactorings.