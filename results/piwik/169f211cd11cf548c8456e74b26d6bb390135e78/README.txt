commit 169f211cd11cf548c8456e74b26d6bb390135e78
Author: mattpiwik <matthieu.aubry@gmail.com>
Date:   Thu Sep 22 09:22:02 2011 +0000

    Fixes #2676
     * Now tracking custom variables with empty value in setEcommerceView
     * Now aggregating custom variables with empty value under a label "Value not defined" (in english always)
     * Updated tests and refactored code in API instead of archiving

    git-svn-id: http://dev.piwik.org/svn/trunk@5203 59fd770c-687e-43c8-a1e3-f5a4ff64c105