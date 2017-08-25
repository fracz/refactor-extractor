commit 13ddf8100f7b4aace962598a75aadbf228c4aaa7
Author: François Kubler <francois@kubler.org>
Date:   Tue May 17 22:34:31 2011 +0200

    New installer.

    * Forms have been revamped (CSS + javascript),
    * Process has been improved : errors are displayed on the form page,
    * Some changes in the index.php page so that everything related to installation is in lib/setup.php
    * Also added a small function in OC_HELPER class to set input values.

    All these should improve the installation process in terms of ergonomics.
    Well, I do hope so.