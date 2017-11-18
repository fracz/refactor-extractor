commit 2e1e404400937448b1cb67a8c1aad4a6e12b04ee
Author: Kirill Likhodedov <Kirill.Likhodedov@jetbrains.com>
Date:   Mon Dec 10 16:04:51 2012 +0400

    [git] IDEA-75202 Move GitCherryPickAction from GitLogUI into separate class

    * Let GitLogUI provide List<GitCommit> and GitCommitDetailsProvider.
    * The latter: not to provide the whole DetailsCache.
    * Move the action into separate class, refactor a bit.
    * Declare the action in plugin.xml.