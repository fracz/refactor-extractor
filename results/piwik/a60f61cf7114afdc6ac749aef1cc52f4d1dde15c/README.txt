commit a60f61cf7114afdc6ac749aef1cc52f4d1dde15c
Author: mattpiwik <matthieu.aubry@gmail.com>
Date:   Mon Mar 2 00:57:54 2009 +0000

    - fix #561 (Piwik_DataTable_Renderer doesn't use the Piwik_API_Request $request parms)
    refactored and added public method to set all customizations on the Renderer + updating tests

    git-svn-id: http://dev.piwik.org/svn/trunk@944 59fd770c-687e-43c8-a1e3-f5a4ff64c105