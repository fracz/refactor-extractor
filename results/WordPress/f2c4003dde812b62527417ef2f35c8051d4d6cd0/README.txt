commit f2c4003dde812b62527417ef2f35c8051d4d6cd0
Author: Pascal Birchler <pascal.birchler@gmail.com>
Date:   Tue Jul 5 15:37:28 2016 +0000

    Update/Install: Fix plugin updates from the details modal on the Dashboard.

    The plugin details modal has been greatly improved in [37714]. However, Shiny Updates aren't yet implemented on the Dashboard. Therefore, we need to fall back to The Bleak Screen of Sadness. Otherwise nothing happens when trying to install an update from inside the modal.

    Fixes #37131. See #37126.
    Built from https://develop.svn.wordpress.org/trunk@37974


    git-svn-id: http://core.svn.wordpress.org/trunk@37915 1a063a9b-81f0-0310-95a4-ce76da25c4cd