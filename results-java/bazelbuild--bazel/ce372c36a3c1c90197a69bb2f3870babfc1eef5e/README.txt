commit ce372c36a3c1c90197a69bb2f3870babfc1eef5e
Author: Janak Ramakrishnan <janakr@google.com>
Date:   Mon Mar 14 16:19:18 2016 +0000

    Roll-forward of commit 4bf0018ed1cf8616297b951dc03dbde3f0db2503 with code to preserve order of glob matches: Parallelize fetches of symlink file values, subdirectory globs, and subdirectory package lookup values. This should improve change pruning speed when we have to check a glob. It also keeps GlobFunction closer to the contract of Skyframe, because in order to avoid quadratic restarts, it wasn't checking for missing deps between getValue calls.

    --
    MOS_MIGRATED_REVID=117139471