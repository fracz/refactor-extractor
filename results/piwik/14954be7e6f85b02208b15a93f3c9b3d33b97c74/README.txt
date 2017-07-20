commit 14954be7e6f85b02208b15a93f3c9b3d33b97c74
Author: mattpiwik <matthieu.aubry@gmail.com>
Date:   Sun Feb 12 10:42:06 2012 +0000

    Work in progress
     * refactored code & rewrote the command line parameter handling code
     * renamed parameters & updated doc
     * auto detect piwik URL (and use HTTPS URL if force_ssl is set)
     * Do not display the memory usage in the log output, easier on the eyes

    Refs #2327

    git-svn-id: http://dev.piwik.org/svn/trunk@5820 59fd770c-687e-43c8-a1e3-f5a4ff64c105