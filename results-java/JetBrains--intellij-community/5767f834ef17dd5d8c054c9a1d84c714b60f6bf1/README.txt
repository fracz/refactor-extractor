commit 5767f834ef17dd5d8c054c9a1d84c714b60f6bf1
Author: Kirill Likhodedov <Kirill.Likhodedov@jetbrains.com>
Date:   Sat May 12 14:40:08 2012 +0400

    [git] refactoring: root -> GitRepository

    * Use GitRepository instead of root VirtualFile for GitUpdateProcess and GitFetcher.
    * Introduce getRepositoriesFromRoots and getRootsFromRepositories (rename) in GitUtil.