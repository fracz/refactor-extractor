commit 1333d1206ec811b5852358f50dfd8c30de09b482
Author: Kirill Likhodedov <Kirill.Likhodedov@jetbrains.com>
Date:   Fri Feb 3 16:33:58 2012 +0400

    GitBranchPopup UI enhancements: current branch and diverge warning

    1. AbstractPopup#setWarning to display a warning message just below the title.
    2. Remove fake actions "Current branch". Show current branch in the popup footer.
    3. Remove fake action "Branches have diverged". Use AbstractPopup#setWarning instead.
    4. Change wording a bit.
    5. Remove redundant code & warnings, some extract method refactorings in GitBranchPopup().