commit 26c20b75c6214f5e5090ae2f849c20662232e318
Author: Igor Minar <igor@angularjs.org>
Date:   Thu Jun 26 13:48:10 2014 -0700

    revert: chore($resource): refactor confusing case statement

    This reverts commit d50829bcf74318a02bbfdbd3172a2bfb95b9283d.

    This commit introduces a regression that results in urls with
    parameters being incorrectly generated. We need to investigate
    further why this is happening, for now I'm just reverting.