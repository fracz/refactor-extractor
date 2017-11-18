commit 95069ee9fde94cce34dd4c4b3ce36b148145e53c
Author: Anton Tarasov <anton.tarasov@jetbrains.com>
Date:   Wed Mar 23 17:59:14 2016 +0300

    IDEA-149337 UI fonts are too big

    The logic of detecting the default system font size on Linux is improved. The default font size is used to derive the IDEA UI scale factor.
    Another side of improvement goes into our custom Linux JDK. GTK L&F will be able to detect "Xft.dpi" value in any Linux Desktop Environment, not necessarily built on GTK (e.g. KDE).
    "Xft.dpi" value is an X server resource which defines the fonts DPI. The value is set by a Linux DE automatically, or in response to users's settings.

    With the new custom JDK, IDEA will get properly scaled font on virtually all Linux's we support. So, the logic of deriving the UI scale will be unified with what IDEA does on Windows.

    With Oracle JDK, IDEA will behave the same way on GTK Linux DE's, but on other Linux's it will fallback to the old approach of detecting the UI scale based on the X server DPI (if set).