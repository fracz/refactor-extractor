commit 088ee712225f45c239dd5e297a2ee24c80080c14
Author: Norman Maurer <norman_maurer@apple.com>
Date:   Fri Nov 27 13:48:32 2015 +0100

    Remove unused method in SslContext

    Motivation:

    We missed to remove a method in SslContext while refactored the implementation. We should remove the method to keep things clean.

    Modifications:

    Remove unused method.

    Result:

    Code cleanup.