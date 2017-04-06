commit a097aa95b7c78beab6d1b7d521c25f7d9d7843d9
Author: Caitlin Potter <caitpotter88@gmail.com>
Date:   Tue Dec 9 10:42:09 2014 -0500

    fix(orderBy): do not try to call valueOf/toString on `null`

    8bfeddb5d671017f4a21b8b46334ac816710b143 added changes to make relational operator work as it
    normally would in JS --- unfortunately, this broke due to my failure to account for typeof null
    being "object".

    This refactoring attempts to convert object values to primitives still, in a fashion similar to
    the SortCompare (and subsequently the ToString() algorithm) from ES, in order to account for `null`
    and also simplify code to some degree.

    BREAKING CHANGE:

    Previously, if either value being compared in the orderBy comparator was null or undefined, the
    order would not change. Now, this order behaves more like Array.prototype.sort, which by default
    pushes `null` behind objects, due to `n` occurring after `[` (the first characters of their
    stringified forms) in ASCII / Unicode. If `toString` is customized, or does not exist, the
    behaviour is undefined.

    Closes #10385
    Closes #10386