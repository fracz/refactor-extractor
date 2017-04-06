commit 262041382f940457ffe990820c7da8a268056d21
Merge: 4923483 56c1e31
Author: Fabien Potencier <fabien.potencier@gmail.com>
Date:   Tue Apr 3 11:38:13 2012 +0200

    merged branch Tobion/PhpMatcherDumper-Improvement (PR #3763)

    Commits
    -------

    56c1e31 performance improvement in PhpMatcherDumper

    Discussion
    ----------

    performance improvement in PhpMatcherDumper

    Tests pass: yes

    The code generation uses a string internally instead of an array. The array wasn't used for random access anyway.
    I also removed 4 unneeded iterations this way (when imploding, when merging and twice when applying the extra indention). A `preg_replace` could also be saved under certain circumstances by moving it.
    And there was a small code errror in line 139.