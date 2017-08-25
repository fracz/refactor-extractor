commit 189ba183ccac1cf7a2d02756895207c48431f70b
Author: Tom N Harris <tnharris@whoopdedo.org>
Date:   Sat Jun 16 23:02:26 2012 -0400

    Raise an exception on socket errors.

    This is the first step in refactoring the socket reader to be more
    resilient and easier to debug.