commit 037b55733d384c194f7554c832f95a5e566c5884
Author: Michael Hamann <michael@content-space.de>
Date:   Mon Nov 15 22:13:36 2010 +0100

    Indexer improvement: replace _freadline by fgets

    In PHP versions newer than 4.3.0 fgets reads a whole line regardless of
    its length when no length is given. Thus the loop in _freadline isn't
    needed. This increases the speed significantly as _freadline was called
    very often.