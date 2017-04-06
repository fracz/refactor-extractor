commit 3fe3da8794571a1479d884be26a621f06cdb7842
Author: Georgios Kalpakas <kalpakas.g@gmail.com>
Date:   Fri Sep 16 17:25:24 2016 +0300

    fix($compile): set attribute value even if `ngAttr*` contains no interpolation

    Previoulsy, when the value of an `ngAttrXyz` attribute did not contain any interpolation, then the
    `xyz` attribute was never set.

    BTW, this commit adds a negligible overhead (since we have to set up a one-time watcher for
    example), but it is justifiable for someone that is using `ngAttrXyz` (instead of `xyz` directly).

    (There is also some irrelevant refactoring to remove unnecessary dependencies from tests.)

    Fixes #15133

    Closes #15149