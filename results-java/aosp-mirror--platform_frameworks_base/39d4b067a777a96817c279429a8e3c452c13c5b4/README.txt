commit 39d4b067a777a96817c279429a8e3c452c13c5b4
Author: Jim Miller <jaggies@google.com>
Date:   Tue Jan 11 20:30:20 2011 -0800

    Fix 3201849: Use custom Drawable for bg in lockscreen

    This uses a custom drawable to improve the performance of
    rendering a transparent background in LockScreen on devices
    without hardware acceleration.

    Change-Id: I7aae13070d475c3ac19d91ba5c6cb7d2a83a18ce