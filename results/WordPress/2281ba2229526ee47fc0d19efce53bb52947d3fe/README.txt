commit 2281ba2229526ee47fc0d19efce53bb52947d3fe
Author: westi <westi@1a063a9b-81f0-0310-95a4-ce76da25c4cd>
Date:   Thu Mar 8 11:19:53 2012 +0000

    XMLRPC: Intoduce a date generation helper method to improve the dates returned over XMLRPC when we have a 0 date stored for drafts.

    This improves the ability of clients to work with the new wp Post APIs. See #18429 and #19733 props maxcutler.


    git-svn-id: http://svn.automattic.com/wordpress/trunk@20153 1a063a9b-81f0-0310-95a4-ce76da25c4cd