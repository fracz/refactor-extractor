commit 4589eb94a9cabb97dbef7f0c1712c6a83ecf89c0
Author: Tom Klingenberg <tklingenberg@lastflood.net>
Date:   Sat May 30 01:21:07 2015 +0200

    fixed and improved OperatingSystem

    The OperatingSystem class was flawed due to superfluous code. A super-
    fluous boolean cast recently introduced in 5555a78 changed an expression
    result making OperatingSystem to return FALSE always on Windows.

    This has been fixed by removing the superfluous code.

    This fixes magerun operating on Windows.

    Additionally more superfluous code has been removed and the code has been
    streamlined.

    Tests cover negatives as well now. One test that is operating system in-
    dependent has been added, too.