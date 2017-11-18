commit 43969f19f8b21d8499d482501df28c4e0d51a899
Author: Chris Gioran <chris.gioran@neotechnology.com>
Date:   Wed Jun 29 14:47:52 2011 +0300

    o Minor refactoring of call chains in IndexManagerImpl to allow for
    o Added package protected method for find/create indexes
    o Added check for not returning auto indexes by name
    o Auto indexer implementations changed to adapt to above
    o Test case for not throwing IllegalArgumentException on getting auto index by name