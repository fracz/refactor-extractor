commit c0ac4af13bb18a516e26d2ddb94723ef9f970393
Author: Selim Cinek <cinek@google.com>
Date:   Fri Mar 3 15:13:48 2017 -0800

    Improved expand accessibility experience

    The expand action is now on the notification itself
    instead of the expand button, this way a user immediately
    know if it is expanded.
    It also improves the click feedback of the expand button

    Test: add notifications, observe accessibility feedback
    Change-Id: I9c397d839df52d5354d7ae16725ce3e595da19c7
    Fixes: 32322410
    Fixes: 35064589