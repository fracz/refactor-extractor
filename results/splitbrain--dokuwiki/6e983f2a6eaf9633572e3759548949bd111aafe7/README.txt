commit 6e983f2a6eaf9633572e3759548949bd111aafe7
Author: Andreas Gohr <andi@splitbrain.org>
Date:   Sun Apr 22 12:05:19 2007 +0200

    don't capture blocked words in spam check

    In the checkwordblock check, the blocked word isn't used, so there is no need
    to capture it. This might improve the spam check speed (untested).

    See http://forum.dokuwiki.org/thread/752

    darcs-hash:20070422100519-7ad00-61e364816942afdd0f714a703f15ac75c838fccb.gz