commit 4f3c4962cebeec6b73456cbacbf7f219ccb90c85
Author: Ben Coburn <btcoburn@silicodon.net>
Date:   Fri Apr 21 03:22:10 2006 +0200

    bugfix fetch remote media (recache and nocache)

    Fixes a major bug in fetching remote media with 'recache' and improves
    the efficiency of 'nocache'.

    Recache:
      - Used to reload the remote media on EVERY request.
      - Now it behaves as intended and only reloads the remote media
        into the Dokuwiki cache every $conf['cachetime'] time.

    Nocache:
      - No longer stores remote media in the Dokuwiki cache.
      - No longer loads, saves, and forwards remote media -- just redirects.
      - No longer resizes images on the server because the cached results
        can not be reused.
    Overall this is faster for Dokuwiki. The bandwidth usage for
    the 3rd party server is the same (less for Dokuwiki). Page loading
    should also be faster because data is not being forwarded through
    Dokuwiki (and the 3rd part server's cache control headers will be
    respected automatically).

    darcs-hash:20060421012210-05dcb-a6029baa0fad218ace28e0e3c2f442b1ca645a99.gz