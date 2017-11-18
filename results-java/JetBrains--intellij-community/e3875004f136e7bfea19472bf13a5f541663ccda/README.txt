commit e3875004f136e7bfea19472bf13a5f541663ccda
Author: Kirill Likhodedov <Kirill.Likhodedov@gmail.com>
Date:   Thu Sep 8 12:54:57 2011 +0400

    GitMergeConflictResolver refactoring

    1. Use a static inner class Params for tuning GitMergeConflictResolver capabilities and texts
    2. Specify roots in the constructor, not in merge()
    3. Rename justMerge -> mergeNoProceed