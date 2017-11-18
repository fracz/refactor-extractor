commit d94d4ef38db7c57fa373942b7681383317eea048
Author: Martin Furmanski <martin.furmanski@neotechnology.com>
Date:   Thu Jul 13 18:33:25 2017 +0200

    Improve bookmark waiting

    Bookmark waiting was previous based on polling and now instead
    uses wait/notify primitives. This is implemented in the AQOOS
    which now supports waiting for a closed transaction id.

    The highestGapFreeNumber is now also volatile so that visibility
    is ensured. This is not necessary for the new bookmark waiting, but
    potentially for other uses.

    The tests for the TransactionIdTracker were also improved to remove
    cluttering boilerplate and make it more of a unit test rather than
    a mocked up messy gray box of integration.