commit 01ae6e578e6209c8cfe625a92aa8f2392893e71f
Author: Kirill Likhodedov <Kirill.Likhodedov@gmail.com>
Date:   Sat Jun 8 19:56:58 2013 +0400

    [git log] don't preload all details once more + some refactorings

    Don't preload details of all commits in a separate request: we
    already have all details in readNextPart =>
      - make it return List<VcsCommit>
      - use it and populate the cache
      - let VcsCommit extend CommitParents
      - remove some fake commit stuff from the DataLoaderImpl,
        because they are not needed now, but produce complexity.
    * Use Collection<Ref> instead of List<Ref>