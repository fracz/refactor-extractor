commit a3028558aeb2fae7e4d298a78691c9a5e72dc44e
Author: epriestley <git@epriestley.com>
Date:   Tue Mar 27 16:06:13 2012 -0700

    Make includes free to the user (application support component)

    Summary:
    We spend a significant amount of time running includes, even with APC. However, we have rigidly structured includes and can safely run them all in workers before requests occur.

    Right now, requests go like this:

      - Apache spawns a worker.
      - Client sends an HTTP request.
      - Apache interprets it.
      - Apache sees it's ".php", so it hands it off to the PHP SAPI.
      - The PHP SAPI starts the PHP interpreter in the worker.
      - The request is handled, etc.

    Instead, we want to do this:

      - Worker spawns and loads the world.
      - Client sends an HTTP request.
      - Webeserver interprets it.
      - Sees it's a ".php", hands it off to the SAPI.
      - SAPI executes it on a loaded world.

    No SAPIs I know of support this, but I added support to PHP-FPM fairly easily (in the sense that it took me 6 hours and I have a hacky, barely-working mess). Over HTTP (vs HTTPS) the performance improvement is pretty dramatic.

    HPHP doesn't significantly defray this cost so we're probably quite a bit faster (to the user) under nginx+PHP-FPM than HPHP after this works for real.

    I have the php-fpm half of this patch in a messy state, I'm going to try to port it to be vs php 5.4.

    Test Plan: Ran a patched php-fpm, browsed around, site works, appears dramatically faster.

    Reviewers: btrahan

    Reviewed By: btrahan

    CC: aran, epriestley

    Differential Revision: https://secure.phabricator.com/D2030