commit 5daf12dba4797d3f6b10ed3837bb98dbdd958817
Author: Konstantin Obenland <obenland@automattic.com>
Date:   Fri Jun 5 05:01:25 2015 +0000

    Text improvements for screen readers in list table paginations.

    Removes title attributes where reasonable and uses accessible hidden text.
    Screen readers will now correctly read out all available information.

    Props afercia, rianrietveld.
    Fixes #32028.


    Built from https://develop.svn.wordpress.org/trunk@32693


    git-svn-id: http://core.svn.wordpress.org/trunk@32663 1a063a9b-81f0-0310-95a4-ce76da25c4cd