commit a79b2f361e875b9feb9db7fd7f26c5384a5339b6
Author: peter <peter@jetbrains.com>
Date:   Tue Oct 25 10:49:23 2016 +0200

    lookup preview fixes

    don't invoke runForEachCaret recursively
    don't show preview when there's selection or in-place refactoring without selection