commit d21f2adb98bf87bca8190f86888a3a8399397322
Author: Scott Mitchell <scott_mitchell@apple.com>
Date:   Sat Apr 22 10:19:28 2017 -0700

    HTTP/2 StreamByteDistributor improve parameter validation

    Motivation:
    Each StreamByteDistributor may allow for priority in different ways, but there are certain characteristics which are invalid regardless of the distribution algorithm. We should validate these invalid characteristics at the flow controller level.

    Modifications:
    - Disallow negative stream IDs from being used. These streams may be accepted by the WeightedFairQueueByteDistributor and cause state for other valid streams to be evicted.
    - Improve unit tests to verify limits are enforced.

    Result:
    Boundary conditions related to the priority parameters are validated more strictly.