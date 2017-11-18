commit 56e633e345a2d82773d1915be60faecd0694ac30
Author: Mikhail Glukhikh <mikhail.glukhikh@jetbrains.com>
Date:   Thu Apr 27 13:34:24 2017 +0300

    CFA: detect captured writes more precisely

    So #KT-14381 Fixed
    So #KT-13597 Fixed
    Also refactors captured writes detection inside DFA