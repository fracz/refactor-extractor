commit 28d297f126e4522ab70e1aca81309c5a5c58e376
Author: Steve Clay <steve@mrclay.org>
Date:   Thu Apr 13 22:53:15 2017 -0400

    feature(messaging): improves admin notices and system messages

    Notices appear outside admin area.
    Messages dismiss more responsively.
    Admin notice CSS moved to own view.

    Fixes #10917

    BREAKING CHANGE:
    `elgg_get_admin_notices()` accepts only an array.