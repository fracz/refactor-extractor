commit 732bf58570cbba1f082d67956821cd0f8d459887
Author: Chris Beams <cbeams@vmware.com>
Date:   Wed Nov 16 04:21:12 2011 +0000

    Rename @CacheDefinitions => @Caching

    Also eliminate all 'cache definition' language in favor of
    'cache operation' in comments, method and parameter names (most
    classes had already been refactored to this effect).