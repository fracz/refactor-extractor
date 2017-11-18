commit 658130843e76483cc7cd0d309f6a0bca387efced
Author: Kirill Likhodedov <Kirill.Likhodedov@gmail.com>
Date:   Mon Oct 7 20:27:36 2013 +0400

    [log] Show branches in BranchesPanel instead of all refs.

    This improves performance of sorting references when there are
    many tags in the repository.

    Better grouping capabilities will be provided later.