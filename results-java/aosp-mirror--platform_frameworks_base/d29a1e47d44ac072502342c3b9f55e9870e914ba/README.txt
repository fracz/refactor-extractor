commit d29a1e47d44ac072502342c3b9f55e9870e914ba
Author: Casey Burkhardt <caseyburkhardt@google.com>
Date:   Thu Feb 12 14:07:55 2015 -0800

    Support circular magnification frame on circular devices

    The magnification viewport expects its boundary to be a rectangular region,
    and always draws it as such.  This change causes the indicator to draw as a
    circle on devices with circular displays.  This also refactors the width of
    the indicator's frame to use a proper dimension resource and updates the
    width to 4dip.

    Bug:18242438
    Change-Id: I1d86647b6d1ef84f5dd506f4141223ec050a79b5