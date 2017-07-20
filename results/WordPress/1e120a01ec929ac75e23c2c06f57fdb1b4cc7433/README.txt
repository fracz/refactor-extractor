commit 1e120a01ec929ac75e23c2c06f57fdb1b4cc7433
Author: Mike Schroder <mschrode@umich.edu>
Date:   Wed Mar 9 04:44:26 2016 +0000

    Media: Progressive enhancement for Imagick; add profiles to whitelist.

    - Progressive enhancement for optional compression improvements and stripping meta.
    - Whitelist IPTC and XMP profiles to maintain Copyright and Rights Usage Terms.
    - Whitelist EXIF profile to maintain orientation information. If handled on upload in the future, it can be stripped as well.

    Fixes #33642. See #28634.
    Props joemcgill, juliobox, ahockley, markoheijnen, adamsilverstein, wonderboymusic, mikeschroder.
    Built from https://develop.svn.wordpress.org/trunk@36891


    git-svn-id: http://core.svn.wordpress.org/trunk@36858 1a063a9b-81f0-0310-95a4-ce76da25c4cd