commit c3f42c102422f70f5bbe67105e16515ce9c306a3
Author: Jason Monk <jmonk@google.com>
Date:   Fri Feb 5 12:33:13 2016 -0500

    Add support for auto-adding tiles

    hotspot, color inversion, data saver, and work profiles should add
    themselves when they first become applicable.

    Also refactor the availability flow a little bit.

    Change-Id: Iaed89059540a98fefd4d26ce69edc0dde987b992