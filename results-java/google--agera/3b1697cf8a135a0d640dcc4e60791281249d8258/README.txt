commit 3b1697cf8a135a0d640dcc4e60791281249d8258
Author: Magnus Ernstsson <magnus@ernstsson.net>
Date:   Tue May 31 14:06:04 2016 +0100

    Tidied up BaseObservable and improved performance (#59)

    Changed BaseObservable to use a private lock
    Changed BaseObservable to sendUpdate to read more easily

    Use a boolean and multimap to track update from/to observable/updatable
    instead of using the much slower handler.hasMessage method

    Also removed the check for myLooper() in sendUpdate that slows down
    updates (especially from other threads). This makes an update execute
    faster but one cycle later, instead of slower, but this cycle