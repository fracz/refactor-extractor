commit 3c30d8949a07d025601589b75593325cf0505d85
Author: Alexander Zolotov <alexander.zolotov@jetbrains.com>
Date:   Fri May 27 20:04:26 2016 +0300

    Live Templates: do not finish template on Esc if completion lookup is active

    + remove redundant EscapeHandler for inplace refactorings
    + finish template if completion list doesn't contain any variants