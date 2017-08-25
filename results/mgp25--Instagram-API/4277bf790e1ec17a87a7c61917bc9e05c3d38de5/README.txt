commit 4277bf790e1ec17a87a7c61917bc9e05c3d38de5
Author: SteveJobzniak <SteveJobzniak@users.noreply.github.com>
Date:   Fri May 26 21:52:48 2017 +0200

    Instagram: Refactoring, grouped story functions

    This is the first step of refactoring the ~5000 lines of code in
    Instagram.php, since it has become way too messy for us to handle.

    It will be done in multiple steps.

    Most story functions have been moved now. To $ig->story->...

    Other functions will be moved to other namespaces too.