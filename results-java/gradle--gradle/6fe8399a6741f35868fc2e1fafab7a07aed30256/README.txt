commit 6fe8399a6741f35868fc2e1fafab7a07aed30256
Author: Cedric Champeau <cedric@gradle.com>
Date:   Fri Mar 4 14:42:26 2016 +0100

    Use the script hash as the key for caching build scripts.

    This commit allows the build scripts to be cached based on their (hash, classpath), instead of their path/uri. This allows multiple improvements:
       - locking is more fine grained as we only need to keep a lock on the cache for compilation of the script
       - 2 identical build scripts in different builds would hit the same cache