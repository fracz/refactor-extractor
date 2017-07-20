commit 4c50e6ec2e6241f6bc0d2758e0a741503a414de7
Author: mattpiwik <matthieu.aubry@gmail.com>
Date:   Thu Feb 2 08:44:26 2012 +0000

    Refs #1465
     Kuddos cappedfuzz for the great refactoring!

    Fixes bug that a subset of the previous tests were ignored.
    FYI: To find out these missing tests, I deleted all files in processed/ directory, then ran all_tests.php - I then noticed maybe 40 files or so, that were in expected/ but not in processed/ meaning they were not generated but they should have been.

    git-svn-id: http://dev.piwik.org/svn/trunk@5743 59fd770c-687e-43c8-a1e3-f5a4ff64c105