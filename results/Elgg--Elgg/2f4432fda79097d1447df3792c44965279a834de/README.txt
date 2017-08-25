commit 2f4432fda79097d1447df3792c44965279a834de
Author: Steve Clay <steve@mrclay.org>
Date:   Tue May 6 19:53:26 2014 -0400

    chore(tests): persistent login tests are no longer fragile

    The persistent login component unit tests were failing when run close to
    the border between seconds on the clock because ElggCookie::setExpiresTime
    depends on the current time. I refactored to allow injecting the current
    time into the persistent login component.

    Fixes #6829