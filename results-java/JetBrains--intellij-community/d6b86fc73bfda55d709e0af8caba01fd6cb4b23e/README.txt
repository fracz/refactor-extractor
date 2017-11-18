commit d6b86fc73bfda55d709e0af8caba01fd6cb4b23e
Author: Kirill Likhodedov <Kirill.Likhodedov@jetbrains.com>
Date:   Sat Mar 30 13:16:26 2013 +0400

    [vcs] "Add to VCS" action: minor refactoring

    Let getUnversionedFiles return not-null list, just empty in case if
    there are no unversioned files.