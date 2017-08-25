commit b920f888ae6efab4febfd5138684e91621ea8dd8
Author: Vincent Petry <pvince81@owncloud.com>
Date:   Wed Oct 29 12:22:50 2014 +0100

    Fix moving share keys as non-owner to subdir

    This fix gathers the share keys BEFORE a file is moved to make sure that
    findShareKeys() is able to find all relevant keys when the file still
    exists.

    After the move/copy operation the keys are moved/copied to the target
    dir.

    Also: refactored preRename and preCopy into a single function to avoid
    duplicate code.