commit 5557581fe58c97d24950406fcc501cad9e2604c2
Author: Kirill Likhodedov <kirill.likhodedov@jetbrains.com>
Date:   Mon Oct 18 12:40:02 2010 +0400

    Git: fixed history problem with null bytes in git log --pretty (IDEA-59554). Refactored GitHistoryUtils.
    Introduced GitLogParser which knows how to parse git log output for the format given (in friendly way) to its constructor. And GitLogRecord which is a friendly representation of information of one revision.
    For GitChangeUtils didn't refactor - just changed %x00 to %x03.
    More tests for GitHistoryUtils.