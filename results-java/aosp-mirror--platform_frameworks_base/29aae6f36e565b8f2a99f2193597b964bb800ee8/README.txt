commit 29aae6f36e565b8f2a99f2193597b964bb800ee8
Author: Dianne Hackborn <hackbod@google.com>
Date:   Thu Aug 18 18:30:09 2011 -0700

    Fix issue #4279860: previous UI flashes before showing lock screen...

    ...(when turning display on after recently turning it off)

    Also clean up when we decide to turn the screen on to improve that
    transition.  There are still problems here with turning it on
    before the wallpaper gets dispayed.

    Change-Id: I2bc56c12e5ad75a1ce5a0546f43a845bf0823e66