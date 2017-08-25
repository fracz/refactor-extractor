commit d19cf2bf564b56d01deb37f538fb1acc7e52aea9
Author: Steve Clay <steve@mrclay.org>
Date:   Sun Nov 30 16:55:15 2014 -0500

    deprecate(access): deprecates elgg_get_access_object() and refactors access lib

    This moves the access object and access cache to be served from the service
    provider, and refactors AccessCollections to remove the need for $CONFIG.