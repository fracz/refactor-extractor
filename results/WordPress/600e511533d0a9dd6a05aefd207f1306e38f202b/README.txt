commit 600e511533d0a9dd6a05aefd207f1306e38f202b
Author: Pascal Birchler <pascal.birchler@gmail.com>
Date:   Tue Jul 5 15:32:28 2016 +0000

    Update/Install: Fix plugin updates from the details modal on `update-core.php`.

    The plugin details modal has been greatly improved in [37714]. However, Shiny Updates aren't yet implemented on the WordPress Updates screen. Therefore, we need to fall back to The Bleak Screen of Sadness. Otherwise nothing happens when trying to install an update from inside the modal.

    Props Nikschavan.
    Fixes #37126.
    Built from https://develop.svn.wordpress.org/trunk@37973


    git-svn-id: http://core.svn.wordpress.org/trunk@37914 1a063a9b-81f0-0310-95a4-ce76da25c4cd