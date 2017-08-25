commit e5e503830f067ce7305e22eac58c78c2f4a007d2
Author: Michael Hamann <michael@content-space.de>
Date:   Mon Nov 15 22:16:33 2010 +0100

    Indexer improvement: Only write the words index when needed

    This adds a simple boolean variable that tracks if new words have been
    added. When editing a page in many cases all words have already been
    used somewhere else or just one or two words are new. Until this change
    all words indexes read were always written, now only the changed ones
    are written. The overhead of the new boolean variable should be low.