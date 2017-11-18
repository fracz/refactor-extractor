commit c7bf48e5e5ade896b410e416d80e1d70e73e0bac
Author: Cedric Champeau <cedric@gradle.com>
Date:   Wed Feb 1 13:44:27 2017 +0100

    Improve caching of module exclusions

    Since we know that `IntersectionExclusion` will always be created with immutable sets, we don't need to create a copy,
    which significantly improves caching because we don't need to duplicate the set and re-compute the hash each time
    the key is queried.