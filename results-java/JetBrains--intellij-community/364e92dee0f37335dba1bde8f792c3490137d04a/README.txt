commit 364e92dee0f37335dba1bde8f792c3490137d04a
Author: Kirill Likhodedov <Kirill.Likhodedov@jetbrains.com>
Date:   Thu Feb 11 19:44:23 2016 +0300

    git: GitRepositoryFiles refactoring

    * Simplify the constructor.
    * Return Files in get- methods to simplify clients' lifes.
    * Get rebase- dirs from GitRepositoryFiles.