commit 462a02b3c68d47fd29434c682e98145ba2e4289a
Merge: 795a240 aae5fb1
Author: Fabien Potencier <fabien.potencier@gmail.com>
Date:   Tue Dec 13 09:06:17 2016 +0100

    feature #20869 [Console] Improve UX on not found namespace/command (Seldaek)

    This PR was merged into the 3.3-dev branch.

    Discussion
    ----------

    [Console] Improve UX on not found namespace/command

    | Q             | A
    | ------------- | ---
    | Branch?       | master
    | Bug fix?      | no
    | New feature?  | yes
    | BC breaks?    | no
    | Deprecations? | no
    | Tests pass?   | yes
    | Fixed tickets | -
    | License       | MIT

    This improves the DX/UX when you don't remember what a command is called.. Traditionally you get this message saying "command x is ambiguous (Y, Z or 6 more)" and if the one you are looking for is in the 6 more you are out of luck. You then have to run the console without arg again, get 50 commands displayed, then have to scroll up to find which one it is you meant.

    With this patch you get all suggestions always, even with description, so you can make an informed decision right away. See before/after on the screenshot below.

    ![image](https://cloud.githubusercontent.com/assets/183678/21080350/c3d446ea-bfac-11e6-934b-ba3d7c3dd34d.png)

    Commits
    -------

    aae5fb1 Improve UX on not found namespace/command