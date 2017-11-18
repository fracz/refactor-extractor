commit 15733d1f8592de90823012570fd8db4fd3408f14
Author: Selim Cinek <cinek@google.com>
Date:   Tue Jul 11 13:19:59 2017 +0200

    Reducing bitmap sizes in notifications

    Bitmap sizes could be arbitrary large when they were sent
    over to the system. We're now reducing them to reasonable
    sizes.s

    Also fixed that notification bitmaps were not put into
    ashmem anymore since it got lost in a refactor.

    Test: code inspection
    Bug: 62319200
    Merged-In: I87db7656e749666b9eab1f67fd497f155c407e18
    Change-Id: I87db7656e749666b9eab1f67fd497f155c407e18