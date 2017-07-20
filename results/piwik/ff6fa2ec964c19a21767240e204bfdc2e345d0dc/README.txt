commit ff6fa2ec964c19a21767240e204bfdc2e345d0dc
Author: mattpiwik <matthieu.aubry@gmail.com>
Date:   Tue Feb 7 21:15:51 2012 +0000

    Refs #534
     * To ensure that the "label filtering" works on all types of labels including special characters "' <> etc.  I refactored the SafeDecodeLabel filter function.
     * Changed DataTableLabelFilter to use Piwik_API_ResponseBuilder directly rather than duplicating code
     * Added few tests cases



    git-svn-id: http://dev.piwik.org/svn/trunk@5774 59fd770c-687e-43c8-a1e3-f5a4ff64c105