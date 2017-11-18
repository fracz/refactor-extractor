commit 08c5ee7dd1a4536eff3c9b480cfcef8909685d6b
Author: Grzegorz Kokosiński <g.kokosinski@gmail.com>
Date:   Mon Oct 26 09:35:15 2015 +0200

    Upgrade airbase version to 46

     - As new airbase comes with new version of testng some tests had to be
    refactored as they started to fail.
     - Upgrade also airlift, discover-server and resolver to versions which
     also are using airbase at version 46, as with older version there were
     a dependency conflict.