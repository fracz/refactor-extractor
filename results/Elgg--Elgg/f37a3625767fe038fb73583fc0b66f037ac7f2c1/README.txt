commit f37a3625767fe038fb73583fc0b66f037ac7f2c1
Author: Steve Clay <steve@mrclay.org>
Date:   Sun Nov 30 16:58:53 2014 -0500

    chore(access): refactors lib/access and AccessCollections to use services

    Also fixes the phpdoc on $CONFIG->site_id and makes it clear this is just a
    copy of $CONFIG->site_guid.