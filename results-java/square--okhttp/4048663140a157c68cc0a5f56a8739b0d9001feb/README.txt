commit 4048663140a157c68cc0a5f56a8739b0d9001feb
Author: Neil Fuller <nfuller@google.com>
Date:   Fri Jan 23 09:51:36 2015 +0000

    Private cache improvements

    Commit 112f020c411c9d14f34e480b98325777b25a7434 changed
    OkHttp's cache from being shared to private cache.

    http://www.w3.org/Protocols/rfc2616/rfc2616-sec13.html#sec13.4
    - permits caching of redirects for "private" responses.

    http://www.w3.org/Protocols/rfc2616/rfc2616-sec14.html#sec14.9.3
    "The s-maxage directive is always ignored by a private cache."
    - the s-maxage check has been removed from the redirect caching
      checks too.