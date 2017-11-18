commit 7085a303822cda26d60e627ffefdaeaf693c5523
Author: Arjen Poutsma <apoutsma@pivotal.io>
Date:   Mon Jun 12 11:03:33 2017 +0200

    Improve capacity calculcation in DefaultDataBuffer

    This commit improves the capacity calculation for the DefaultDataBuffer,
    so that the capacity typically doubles instead of improving by the
    minimal required amount.

    Issue: SPR-15647