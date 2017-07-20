commit 0abe2b8e7de4ada6d1d8c9e693e6c17dbe986bbc
Author: Thomas Steur <thomas.steur@gmail.com>
Date:   Wed Oct 23 21:14:58 2013 +0000

    refs #4126 refactored plugin settings to work with User and System setting data objects, the plugin settings does not know anything abotu User/System settings so it is possible to add different kind of settings in the future. Also added nonce when saving settings