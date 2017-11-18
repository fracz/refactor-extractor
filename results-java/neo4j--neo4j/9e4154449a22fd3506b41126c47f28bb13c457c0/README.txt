commit 9e4154449a22fd3506b41126c47f28bb13c457c0
Author: Anton Klaren <anton.klaren@neotechnology.com>
Date:   Wed Sep 20 14:06:57 2017 +0200

    Removed grandparent and parent-central

    This is a large refactoring of the POM structure which removes a lot of cruft that has been accumulated though the years.

     - Most notably the lifecycle now align with the regular maven lifecycle.
     - The -DrunITs is removed and integration tests are not opt-out instead of opt-in.
     - Duplicate declarations in child POMs has moved up to the parent POMs instead.
     - All eclipse m2e declarations is removed.
     - Old obsolete properties no longer in used is removed.

    The current caveat is that javadoc jar and source jar file generation is most probably broken. Since this hard to test/verify without running it through the build system I find it's a necessary break. And given that fact that the release of 3.4 is far away is shouldn't be a problem.