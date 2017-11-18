commit a15001d2115da19a9872b823841d2ba274fef57f
Author: Mikhail Golubev <mikhail.golubev@jetbrains.com>
Date:   Thu Nov 27 18:24:04 2014 +0300

    "Move file/symbol" refactoring does not permute updated imports

    Previously new imports were always inserted after all other imports in
    containing statement list, even if they were intended to replace
    another existing import. I've changed meaning of "anchor" parameter a
    bit, so that it can be used to specify exact insertion place for
    new import.