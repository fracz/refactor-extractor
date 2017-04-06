commit aa75adf8bbdedd8f15bf459b143ad4aa6e5b936d
Merge: 57f8d1e 57008ea
Author: Fabien Potencier <fabien.potencier@gmail.com>
Date:   Sat Oct 22 07:30:10 2016 -0700

    bug #20239 [HttpKernel] Fix a regression in the RequestDataCollector (jakzal)

    This PR was merged into the 3.1 branch.

    Discussion
    ----------

    [HttpKernel] Fix a regression in the RequestDataCollector

    | Q             | A
    | ------------- | ---
    | Branch?       | 3.1
    | Bug fix?      | yes
    | New feature?  | no
    | BC breaks?    | no
    | Deprecations? | no
    | Tests pass?   | yes
    | Fixed tickets | #19701
    | License       | MIT
    | Doc PR        | -

    The regression was introduced by refactoring made as part of #17589 (if/else statements where rearranged).

    Commits
    -------

    57008ea [HttpKernel] Fix a regression in the RequestDataCollector
    26b90e4 [HttpKernel] Refactor a RequestDataCollector test case to use a data provider