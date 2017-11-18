commit 6dba070ed9094c75406bd86a007778aa5b30958e
Author: Nadya Zabrodina <Nadya.Zabrodina@jetbrains.com>
Date:   Tue Dec 2 21:33:21 2014 +0300

    IDEA-133562 Force push warning: add "don't warn again for this branch" checkbox

    * branchName added in HgTarget to improve equals method;
    * confirmation should be shown if targets are different or if not remembered;
    * checkBox should not be shown for different targets;
    * annotations added