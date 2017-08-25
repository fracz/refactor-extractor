commit 155fffe6a4db4dc16d7dad86e7988a39f977c1b7
Author: David Mudrak <david@moodle.com>
Date:   Tue Jul 5 10:13:14 2011 +0200

    MDL-28168 Improved custom_menu constructor

    This improves custom_menu constructor by dropping support for useless
    $text parameter and replacing it with the menu definition. This makes
    custom_menu instance independent on the place where the menu is defined
    ($CFG->custommenuitems) and can be unit-tested. Also, multiple instances
    of custom_menu can be instantiated now which can be interesting in the
    future.