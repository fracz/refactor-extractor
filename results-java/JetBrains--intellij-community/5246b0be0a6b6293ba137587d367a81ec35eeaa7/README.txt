commit 5246b0be0a6b6293ba137587d367a81ec35eeaa7
Author: Nadya Zabrodina <Nadya.Zabrodina@jetbrains.com>
Date:   Tue Jul 30 14:42:54 2013 +0400

    HgVersion and validation refactoring

    *HgVersion class created to update and parse hg version or throw appropriate exception;
    *update and store actual HgVersion in HgVcs (in activate meth and after saving config in panel);
    *test for version parsing added;
    *hg validation changed: perform validation without hg version command (see gitExecutableValidator);
    *annotations added;
    *links to config settings and hg update added to error message.