commit 576bdf9f8c19c387963795229444ca716b125974
Author: Andrea Fercia <a.fercia@gmail.com>
Date:   Sat Sep 17 15:39:30 2016 +0000

    Accessibility: Standardize the remove/delete/cancel links in the Menus screen and Publish meta boxes.

    The `submitdelete` CSS class is used in various places across the admin for some
    "red" action links. It is worth simplifying this rule for further improvements
    related to color contrast.

    - simplifies a non-standard styling for the "Remove/Cancel" links in the Menus screen
    - underlines all the "Move to trash/Delete" red links in all the Publish meta boxes
    - fixes CSS classes usage for all the Publish meta boxes primary buttons
    - fixes broken layout for the old Link Manager publish meta box

    Props karmatosed, hugobaeta, monikarao, afercia.
    Fixes #37969, #37018. See #37448, #37138, #27314.

    Built from https://develop.svn.wordpress.org/trunk@38616


    git-svn-id: http://core.svn.wordpress.org/trunk@38559 1a063a9b-81f0-0310-95a4-ce76da25c4cd