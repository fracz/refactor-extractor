commit c7106d4f6c0f550ad401ef8d8b3df2a5ec7eeea0
Author: Lukacs Berki <lberki@google.com>
Date:   Thu Oct 15 07:45:54 2015 +0000

    Make recursive package wildcards work in remote repositories.

    Ideally, PrepareDepsOfPatternFunction and maybe even RecursivePkgFunction would also be changed to take a PackageIdentifier instead of RootedPath because the less places we store the set of roots, the better, but I've done enough refactoring in the past weeks to not be thrilled by the idea of doing more.

    --
    MOS_MIGRATED_REVID=105486561