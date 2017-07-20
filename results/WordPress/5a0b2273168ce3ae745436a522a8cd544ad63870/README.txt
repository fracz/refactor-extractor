commit 5a0b2273168ce3ae745436a522a8cd544ad63870
Author: Andrew Nacin <wp@andrewnacin.com>
Date:   Mon Oct 21 22:29:09 2013 +0000

    Rename the automatic_updates_send_email filter to auto_core_update_send_email. (Final name change.)

    fixes #25653. Also:
     * Fully document all new hooks, methods, and classes in the upgrader.
     * Rename 'language' to 'translation' inside the upgrader.
     * Improve the readability of the crazy do-while loop in the is_vcs_checkout() method.

    Built from https://develop.svn.wordpress.org/trunk@25859


    git-svn-id: http://core.svn.wordpress.org/trunk@25771 1a063a9b-81f0-0310-95a4-ce76da25c4cd