commit 28dd2513a2e7cfd4be28c0d287c44a262214093b
Author: poltawski <poltawski>
Date:   Mon Aug 3 16:56:53 2009 +0000

    block/rss_client: MDL-19990 Make feed discovery configurable

    Simplepie will try quite aggresively to autodiscover rss feeds by
    default. Make this configurable when adding a feed and also store
    the autodiscovered location rather than the input location.

    I also removed the feed validator as this doesn't make much sense with
    feed autodiscovery, and I think we can improve simplepie integration to
    do this instead