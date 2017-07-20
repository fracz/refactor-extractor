commit 84ad17f73b6b75ff569db3e053830b9406582b70
Author: Dries Buytaert <dries@buytaert.net>
Date:   Mon Dec 30 23:30:55 2002 +0000

    Patch by Michael:

    admin.css:

    * Removed border-bottom for menu list items. The only work around to retain
    the border-bottom on lists with nested items is to use divs rather than li
    tags as far as I know. I think the list items are still readable this way.
    * Made title of site in sidenav more differentiated from menu.
    * Made color changes so that all headings are the same hue (navy bluish).
    * Modified th text-align: left; for better readability. Modified
    border-bottom: 1px solid #ccc; Black addded visual noise.
    * Modified tr.dark background-color: #ddd, tr.light background-color: #fff;
    * Changed all colors to #xxx three number hex shortcuts.
    * Added hr rule to make horizontal rules have height: 1px; color: #ccc;

    admin.php:

    * Added 1 line to admin.php to include <link rel="stylesheet" ...>. The
    stylesheet refers to a misc/print.css stylesheet which may not exist in your
    misc/ directory. This fixes the flash of unstyled content that users of Win
    IE 5+ are experiencing. You can use this to specify print styles if you
    like. See more about this here: http://www.bluerobot.com/web/css/fouc.asp