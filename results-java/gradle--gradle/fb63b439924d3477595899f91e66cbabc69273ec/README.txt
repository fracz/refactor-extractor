commit fb63b439924d3477595899f91e66cbabc69273ec
Author: Cedric Champeau <cedric@gradle.com>
Date:   Tue Jul 18 16:34:40 2017 +0200

    Use an explicit validation mutator instead of relying on a proxy

    This doesn't make the fix any better (well, not using a proxy is an
    improvement, it's faster, safer...), because we still need to shuffle
    types around. We shouldn't have to worry about 2 different mutation
    validation types.