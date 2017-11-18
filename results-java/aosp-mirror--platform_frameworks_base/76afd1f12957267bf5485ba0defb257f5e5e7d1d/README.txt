commit 76afd1f12957267bf5485ba0defb257f5e5e7d1d
Author: Adam Lesinski <adamlesinski@google.com>
Date:   Wed Oct 23 10:45:28 2013 -0700

    Improve Jank for translucent activities

    The previous jank improvement only worked when closing
    an app, not when bringing one forward (hitting home button).
    This should cover the specific case that is being missed: Having the
    Home task being brought to front over a translucent window, with
    a wallpaper behind both tasks.

    bug:11253262
    Change-Id: I200ef6fe2dda8d9ab4e1f82059b4f888c59007f4