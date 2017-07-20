commit b3b32f22a17a6c34268063563b356c3878b68b56
Author: Aaron Jorbin <aaron@jorb.in>
Date:   Tue Aug 9 01:14:28 2016 +0000

    Updates: Improve experience for Bulk Actions when FTP is dismissed.

    Before this change, when a bulk update was canceled due dismissing the FTP credentials modal, part of the actions didn't get canceled.  This meant the "There is a new version of…” notices become blank and the updates you had checked became unchecked.  Now, the notices remain and you are essentially returned to the screen you had before. Strings are also updated to improve ARIA usage.

    Fixes #37563.
    Props ocean90, swissspidy, obenland, afercia.


    Built from https://develop.svn.wordpress.org/trunk@38221


    git-svn-id: http://core.svn.wordpress.org/trunk@38162 1a063a9b-81f0-0310-95a4-ce76da25c4cd