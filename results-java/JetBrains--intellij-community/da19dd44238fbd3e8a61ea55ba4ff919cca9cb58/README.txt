commit da19dd44238fbd3e8a61ea55ba4ff919cca9cb58
Author: Kirill Likhodedov <Kirill.Likhodedov@jetbrains.com>
Date:   Tue Jul 7 14:21:44 2015 +0300

    [vcs] VDSM refactoring: get rid of Scopes

    * Operate directly with the dirty builder and return the VcsInvalidated when needed,
      instead of populating Scopes and then requesting it to return data.
    * Make DefaultVcsRootPolicy return a set of dirty roots and then
      handle it as usual: group by vcs and mark dirty.