commit 202c3b64e98cc2831e8b71e6b20b78a86fbb217a
Author: Jim Wigginton <terrafrost@php.net>
Date:   Sun Sep 12 21:58:54 2010 +0000

    - added support for OFB and CFB modes (with the caveat that CFB mode isn't currently supported as a stream cipher)
    - improvements to the fix to the bug Suby found
    - fixed bug whereby CTR mode gave different results in mcrypt and internal modes when the continuous buffer was enabled and the plaintext being encrypted wasn't a multiple of the block size
    - undid the fix for the bug f.dammassa found (thanks, j31!)


    git-svn-id: http://phpseclib.svn.sourceforge.net/svnroot/phpseclib/trunk@120 21d32557-59b3-4da0-833f-c5933fad653e