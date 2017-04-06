commit 84c04b0b6856ee0cc42867bc4e8ab8e583e54d11
Author: ryanhart2 <hart.ryan@live.com.au>
Date:   Fri Feb 12 05:27:30 2016 +0700

    docs($http): improve description of caching

    Included changes:

    * Point out that only GET & JSONP requests are cached.
    * Explain that the URL+search params are used as cache keys (headers not considered).
    * Add note about cache-control headers on response not affecting Angular caching.
    * Mention `$httpProvider.defaults.cache` (in addition to `$http.defaults.cache`).
    * Clear up how `defaults.cache` and `config.cache` are taken into account for determining the
      caching behavior for each request.

    Fixes #11101
    Closes #13003