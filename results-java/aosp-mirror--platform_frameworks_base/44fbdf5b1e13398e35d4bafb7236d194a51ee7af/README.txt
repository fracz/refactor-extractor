commit 44fbdf5b1e13398e35d4bafb7236d194a51ee7af
Author: Wale Ogunwale <ogunwale@google.com>
Date:   Wed Nov 16 10:18:45 2016 -0800

    Fixed issue with IME displaying on-top of nav bar.

    Caused by some recent refactoring. We now make sure the IME
    has the higher animation layer in its base layer of the window
    it is targeting.
    Also, consolidated some of our test functions.

    Bug: 32916670
    Test: bit FrameworksServicesTests:com.android.server.wm.WindowLayersControllerTests
    Change-Id: I0b1abd6fead981cfc810488cc785261abba5341d