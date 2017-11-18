commit 103c25b744500fc6d54b3bb065289b9a7f2dd665
Author: Kirill Likhodedov <Kirill.Likhodedov@jetbrains.com>
Date:   Fri Oct 20 11:24:04 2017 +0300

    Rewrite git cherry pick process to improve stability and support moves

    Previously the process looked like this:
    1. `git cherry-pick -n`
    2. update the ChangeListManager
    3. move the changes from the default changelist to another list,
       created specially for the cherry-pick process.

    This caused several issues:
    1. moving operation is asynchronous and is not very well supported
       by the CLM.
    2. in fact, we don't know which change is to be moved, and looking at
       the changes from the commit, being cherry-picked/reverted,
       doesn't help because of possible moves: IDEA-171085.

    Instead, another procedure is used now:
    1. Create new changelist and set it as default.
    2. `git cherry-pick -n`
    3. update the ChangeListManager (unfortunately, full VFS refresh,
       and full git status rescan is needed - again, because of renames).
    4. deal with the changelist and its changes.
    5. return the previous default changelist.

    This fixes IDEA-171085, IDEA-173158 and IDEA-162716,
    this also should fix issues like IDEA-177128, IDEA-146160 and IDEA-180878.