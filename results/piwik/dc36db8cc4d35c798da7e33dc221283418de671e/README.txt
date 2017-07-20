commit dc36db8cc4d35c798da7e33dc221283418de671e
Author: mattpiwik <matthieu.aubry@gmail.com>
Date:   Tue Jan 11 06:30:27 2011 +0000

    Small performance improvement: using shorter names in all archiving result sets (these queries can return thousands of rows, so avoiding creating more complex than necessary arrays)

    git-svn-id: http://dev.piwik.org/svn/trunk@3700 59fd770c-687e-43c8-a1e3-f5a4ff64c105