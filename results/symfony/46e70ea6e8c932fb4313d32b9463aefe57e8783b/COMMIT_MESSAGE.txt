commit 46e70ea6e8c932fb4313d32b9463aefe57e8783b
Merge: 6659d7d 64a0b40
Author: Fabien Potencier <fabien.potencier@gmail.com>
Date:   Wed Oct 9 22:48:03 2013 +0200

    bug #9257 [Process][2.2] Fix 9182 : random failure on pipes tests (romainneutron)

    This PR was merged into the 2.2 branch.

    Discussion
    ----------

    [Process][2.2] Fix 9182 : random failure on pipes tests

    | Q             | A
    | ------------- | ---
    | Bug fix?      | yes
    | New feature?  | no
    | BC breaks?    | no
    | Deprecations? | no
    | Tests pass?   | yes
    | Fixed tickets | #9182
    | License       | MIT

    I'm not a big fan of this fix, but - at least - it works.

    With this code, finally, Process does not behave the same at all on Windows and Linux.
    This patch does not smells very good but it solves the random failing test issue (that produced at runtime too).

    Actually, calling `proc_get_status` within the waiting loop introduced the bug.
    So this PR reverts to the previous behavior (consider a process running as long as pipes give data). On Windows, this is not the same behavior as we're not using streams but file handles. Whereas the feof of a stream is detected when the other side closes, the feof of a file handle can be reached at any time. So, on Windows, `proc_get_status` is called (checking the feof of the file handle might be positive until the executable outputs something), and we consider a process running as long as the information returned says it's running.

    We could think of decouple windows and linux logic in two separated objects, using the interfaces I introduced in #8753. This could bring much more readability and make the code more easy to understand.

    Commits
    -------

    64a0b40 [Process] Fix #9182 : random failure on pipes tests