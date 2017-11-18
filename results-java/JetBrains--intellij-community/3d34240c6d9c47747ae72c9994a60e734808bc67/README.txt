commit 3d34240c6d9c47747ae72c9994a60e734808bc67
Author: Kirill Likhodedov <Kirill.Likhodedov@jetbrains.com>
Date:   Wed Apr 4 20:21:52 2012 +0400

    [git] Move method

    Move GitMergeUtil.unmergedFiles() to GitConflictResolver, since the latter class is the only client, and for further refactorings.