commit a1302afe75242fe2d1dee1a87f7688b02b1f641e
Author: Jesse Vincent <jesse@fsck.com>
Date:   Tue Dec 15 02:52:01 2009 +0000

    Initial implementation of loading only 25 rows from the message list
    from SQLite at once. The hope is that this will improve perceived
    performance on large folders by starting message display sooner.

    In the case of a background sync while we're loading, we _may_ end up
    doing more work than necessary, since we implement paging by "date
    received"