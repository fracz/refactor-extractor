commit 0767cd7374046ccf5a9353d55b2ec7e7739ed229
Author: mattpiwik <matthieu.aubry@gmail.com>
Date:   Thu Aug 11 22:44:07 2011 +0000

    Refs #2327
     * adding option forceall+reset which does imitate closely the current archive.sh behavior (with still the added bonus)
     Fixes #1938 added segment in lock name. I have tested the code path but haven't actually verifier that this improved performance

    git-svn-id: http://dev.piwik.org/svn/trunk@5102 59fd770c-687e-43c8-a1e3-f5a4ff64c105