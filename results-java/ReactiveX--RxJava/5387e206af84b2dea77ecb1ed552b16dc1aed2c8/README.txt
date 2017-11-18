commit 5387e206af84b2dea77ecb1ed552b16dc1aed2c8
Author: David Karnok <akarnokd@gmail.com>
Date:   Fri Oct 13 22:53:01 2017 +0200

    2.x: improve Flowable.timeout() (#5661)

    * 2.x: improve Flowable.timeout()

    * Remove the now unused FullArbiter(Subscriber)

    * Don't read the volatile twice