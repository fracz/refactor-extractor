commit 9c8d4bbf21f4f2088a42c5cf409f222a9d0133c3
Author: padraic <padraic@44c647ce-9c0f-0410-b52a-842ac1e357ba>
Date:   Sun Sep 20 13:12:59 2009 +0000

    Added Zend_Feed_Reader_FeedSet class extending ArrayObject
    Added support for lazy loading detected feeds from Zend_Feed_Reader::findFeedLinks() method result (FeedSet)
    Added support for retaining all links detected as a FeedSet Array Object
    Maintains backwards compatibility with previous API as a "quick method" when other features not required.
    Implements (among other things) the improvement suggested in ZF-7870


    git-svn-id: http://framework.zend.com/svn/framework/standard/trunk@18318 44c647ce-9c0f-0410-b52a-842ac1e357ba