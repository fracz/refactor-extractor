commit ec3326679595953650e76308daac2182b7d628b9
Author: Dries Buytaert <dries@buytaert.net>
Date:   Fri Oct 31 19:34:03 2003 +0000

    - Improvement: made it possible to disable the comment controls and reworded some of the configuration settings.

    - Improvement: removed a left-over from Drupal 4.2.0 (dead code).

    - Improvement: replaced hard-coded XHTML around the XML icons with class="xml-icon".

    - Improvement: removed the custom navigation menus shown at the top of the "user information page" and integrated them in the new navigation block.  The "my account" link in the navigation menu will unfold.  Also removed the "delete account" link/functionality (for now).

    - Improvement: fix for "magic quotes" settings.  Patch by Steven.  I also removed check_php_settings().

    - Improvement: block themability improvements.  Modified patch from Ax and Steve/CodeMonkeyX.

    - Fixed bug #2617: editing user information does not work.  Patch by Kjartan.