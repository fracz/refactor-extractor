commit f757db63d5c601cc8fc8ae05400ee2a1783324cd
Author: Kirill Likhodedov <Kirill.Likhodedov@jetbrains.com>
Date:   Fri Mar 11 16:42:42 2016 +0300

    git: IDEA-152430 Removing remote branch should offer to delete tracking branch

    This used to work, but was broken a while ago, during some
    refactoring related to using of GitRemoteBranch objects.