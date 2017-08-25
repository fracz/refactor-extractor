commit 791772abea631c503d95e21f584a66b47dc042f4
Author: Nicolai Ehemann <en@enlightened.de>
Date:   Thu Jan 16 17:21:19 2014 +0100

    refactored/cleaned up lib/files.php
    cleaned up get() logic
    fixed get() to only send headers if requested (xsendfile could get in the way)
    do no longer readfile() when already using mod_xsendfile or similar