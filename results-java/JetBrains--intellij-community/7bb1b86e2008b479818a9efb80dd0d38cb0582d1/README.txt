commit 7bb1b86e2008b479818a9efb80dd0d38cb0582d1
Author: Nadya Zabrodina <Nadya.Zabrodina@jetbrains.com>
Date:   Tue Nov 19 14:08:57 2013 +0400

    (IDEA-116490) remove --debug argument if hg supports parents template variables

    *hg log command refactored;
    *appropriate supported version added to HgVersion;
    *construct hashes argument modified for new log