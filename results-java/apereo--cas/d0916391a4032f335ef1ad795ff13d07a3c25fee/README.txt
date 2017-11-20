commit d0916391a4032f335ef1ad795ff13d07a3c25fee
Author: Misagh Moayyed <mmoayyed@unicon.net>
Date:   Tue Sep 12 09:31:40 2017 -0700

    Management webapp service search (#2922)

    * Added Domain screen

    * Added Domain screen

    * Added Domain screen

    * WIP

    * Removed details list

    * WIP- Header refactored

    * Added Search screen
    Refactored header
    Removed cas-management.css dependency
    Made header and tab bar fixed position

    * Made query work on all three fields with regex
    Made search navigate on keyup

    * Revert: Delete of unneeded static resources

    * Change default theme to deeppurple-amber

    * Closes issue #26 - Duplicate functionality not working

    * Changed to deeppurple
    removed fonts

    * Created custom theme
    Stop Form from resolving twice on navigation
    updated Karma package to latest for depency conflict error

    * Fixed logout