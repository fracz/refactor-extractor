commit 9bedc138ca8685b8efcf48a06837e3a4639153da
Author: Kirill Likhodedov <Kirill.Likhodedov@jetbrains.com>
Date:   Wed Apr 4 20:18:54 2012 +0400

    [git] Move method

    Move GitChangeUtils.unmergedFiles() to GitMergeUtil, since the latter class is the only client, and for further refactorings.