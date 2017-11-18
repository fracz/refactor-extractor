commit 16566656cd2f81f62c8693e64de68062f2680161
Author: Neil Fuller <nfuller@google.com>
Date:   Wed Aug 27 17:15:09 2014 +0100

    Fixing android.text.format.Time for non-English locales

    The test fixes for bug 17262063 showed up a real issue for
    non-English locales with the Time.format() method:
    If the Android string resources that contain the pattern use
    non-ASCII characters then a '?' would be output instead of
    those characters.

    For example, in France the pattern for '%c' includes a 'à'
    (a with a grave accent) and Japan includes 日.

    The problem was due to converting the pattern to bytes using
    the US_ASCII character set, which turns non-ASCII characters
    into '?'. The code has been changed to use char throughout
    and avoid bytes.

    Internal documentation has been improved.

    Some calls to modifyAndAppend() have been replaced with a
    direct call to outputBuilder.append() because the
    modify step is guaranteed to a no-op for the literals given.

    The formatter has been changed to use Locale.US because it
    is only used for outputting numbers. It has been renamed
    to make this more obvious and the locale field has been
    removed.

    Bug: 17262063

    (cherry picked from commit 788cb18f652fca380acefdadce305415bc0602b0)

    Change-Id: I96ee158fbeb01827f0bbf022631625416f872fdb