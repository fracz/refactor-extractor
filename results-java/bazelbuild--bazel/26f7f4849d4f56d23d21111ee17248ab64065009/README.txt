commit 26f7f4849d4f56d23d21111ee17248ab64065009
Author: Damien Martin-Guillerez <dmarting@google.com>
Date:   Thu Feb 25 12:14:42 2016 +0000

    Testing correct invalidation of Skylark Remote Repositories

    A Skylark remote repository should be invalidated only when
    the WORKSPACE file change, or one of its dependency or the Skylark
    file change.

    This change include two fixes:
      - The path of the RepositoryDirectoryValue was incorrect when
        the directory root is a symlink and the repository is not local
        (and not refetching). This was never triggered before because
        the only rule that were symlinking their root were the local
        one.
      - Directories were unitialized for the SkylarkRepositoryFunction
        (was forgotten as part of a refactor when introducing it).

    --
    MOS_MIGRATED_REVID=115547540