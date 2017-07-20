commit 805107cd2202ddee66c4743e43804a3069508f29
Author: Dries Buytaert <dries@buytaert.net>
Date:   Mon Apr 2 15:54:37 2001 +0000

    Commiting my work of last Sunday:

     - removed ban.inc and ban.module and integrated it in account.module
       under the name "access control" --> the ban code was not really up
       to standard so this has now been dealt with.  This refactoring and
       reintegration cuts down the code size with 100 lines too.  :-)
       (The ban.module code was really old and it showed.)

     - added node.module and made the other modules reuse some of this
       code --> cut down the code size of modules by at least 100 lines
       and adds stability.

     - added a status() function to admin.php to display a conform status
       message where appropriate.  See admin.php for usage.

     - removed $theme->control() and made comments.inc handle this itself
       wrapped in a $theme->box().  No need to clutter the themes with
       such complexity --> updated all themes already. :-)

     - some small visual changes to some administration pages to be more
       consistent across different modules.