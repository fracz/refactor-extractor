commit 01eb2c543b7fefb27ae67df65e2d19345ffaf808
Author: Malcolm Blaney <mblaney@gmail.com>
Date:   Thu Jun 2 16:32:48 2016 +1000

    New release 1.4.1:
    This was prompted by an inconsistent hash result in SimplePie_Item->get_id().
    Please note that if you relied on this feature, the returned id's will again
    change, but hopefully for the last time.
    Includes other minor bug fixes and improved support for microformats feeds.