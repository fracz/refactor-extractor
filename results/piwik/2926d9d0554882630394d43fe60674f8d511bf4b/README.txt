commit 2926d9d0554882630394d43fe60674f8d511bf4b
Author: mattpiwik <matthieu.aubry@gmail.com>
Date:   Thu Feb 17 07:30:40 2011 +0000

    Fixes #2054, #2076, #1947
     * Visitor Log: offset now removed from Live API all together. As a result, Previous< in the datatable footer will return to the start, this could be improved but OK for now. Offset for next is the ID Visit as before
     * Live! widget: offset is now the timestamp, so the request hits the INDEX
     * This should fix most of the reordering bug: well now it should load on the top, the visits that have been updated since last refresh, and it deletes previous visit rows that are being updated

    git-svn-id: http://dev.piwik.org/svn/trunk@3922 59fd770c-687e-43c8-a1e3-f5a4ff64c105