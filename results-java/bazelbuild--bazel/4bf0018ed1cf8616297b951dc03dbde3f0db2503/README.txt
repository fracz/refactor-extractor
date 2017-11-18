commit 4bf0018ed1cf8616297b951dc03dbde3f0db2503
Author: Janak Ramakrishnan <janakr@google.com>
Date:   Mon Feb 29 19:59:44 2016 +0000

    Parallelize fetches of symlink file values, subdirectory globs, and subdirectory package lookup values. This should improve change pruning speed when we have to check a glob. It also keeps GlobFunction closer to the contract of Skyframe, because in order to avoid quadratic restarts, it wasn't checking for missing deps between getValue calls.

    --
    MOS_MIGRATED_REVID=115882309