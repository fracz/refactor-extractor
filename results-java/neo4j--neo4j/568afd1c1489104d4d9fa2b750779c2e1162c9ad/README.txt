commit 568afd1c1489104d4d9fa2b750779c2e1162c9ad
Author: Lasse Westh-Nielsen <lassewesth@gmail.com>
Date:   Wed Nov 20 16:44:33 2013 +0100

    having GraphStoreFixture _not_ succeed it's transactions. This was changed recently, now changing it back as it seems to give us spurious test failures.
     That it improves the build and makes it less flaky is based on running FullCheckIntegrationTest a number of times on the actual build slave, which is the only place where this flakiness seems to manifest.
     That it makes matters no worse is proved by running the test locally too and seeing no flakes at all, like before.