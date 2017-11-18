commit 1ac23ac9b3a220e4a407258510ca2f96bb8d6b42
Author: Davide Grohmann <davide.grohmann@neotechnology.com>
Date:   Wed Oct 29 11:57:53 2014 +0100

    Use a fair tryLock call in IndexSamplingController

    - fix a flacky test in IndexSamplingController
    - improve handling of InterruptedException in DoubleLatch