commit 9e3fc964b6e9051f54341b0fb70642d130bb9ec7
Author: Kirill Likhodedov <Kirill.Likhodedov@jetbrains.com>
Date:   Tue Oct 16 17:20:13 2012 +0400

    [git] Remove deprecated code from GitBranch

    GitBranch.current(), list(), listAsStrings(): substitute to GitRepository framework.
    Introduce GitBranchUtil.getCurrentBranch() for those cases where GitRepository is not used yet, not to repeat myself with retrieving the instance of GitRepository each time.

    Preserve listAsStrings in LowLevelAccessImpl, because --contains is needed there. But refactor it to return the list of branches instead of filling  once of the parameters.

    Remove some dead code.