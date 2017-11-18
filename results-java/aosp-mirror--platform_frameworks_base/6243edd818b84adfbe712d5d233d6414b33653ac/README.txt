commit 6243edd818b84adfbe712d5d233d6414b33653ac
Author: Amith Yamasani <yamasani@google.com>
Date:   Mon Dec 5 19:58:48 2011 -0800

    New and improved silent mode on lockscreen.

    3-state item to toggle between Silent/Vibrate/Ringer in long-press power menu.
    No volume dialog on lockscreen, unless Power menu is up.

    Set VIBRATE_IN_SILENT=1 when upgrading device.

    Change-Id: I097d216f96c4abdbd83420e0c477106951b3607d