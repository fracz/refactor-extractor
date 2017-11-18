commit ff371cf0ed8ceb547043cd2f7db0cfd0990203b2
Author: Mike Lockwood <lockwood@android.com>
Date:   Wed Sep 22 23:25:33 2010 -0400

    MediaScanner: disable album artist support until MediaProvider really supports it

    Fixes a "no such column" exception in MediaProvider.update() that I somehow
    missed when testing the MediaProvider refactoring.

    Change-Id: Icc502a5c0e3bd150b353972d000b978a9e044abc
    Signed-off-by: Mike Lockwood <lockwood@android.com>