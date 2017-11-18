commit d3ab692d28018825578ff05832644cfad60233fb
Author: Raph Levien <raph@google.com>
Date:   Mon Mar 2 14:30:53 2015 -0800

    Some refactoring of StaticLayout

    This patch refactors construction of StaticLayout to use an explicit
    Builder object, which is intended to hold state used for constructing
    the layout but not needed for merely reading out the results.

    Builder objects are allocated from a pool and explicitly recycled,
    so there is insignificant additional allocation cost.

    This patch has very little impact on performance (it does avoid
    allocating a FontMetricsInt object) but opens the way for significant
    performance and functionality improvements as more of the Builder
    functionality migrates to native code.

    Change-Id: I2a576643e573a38b61f895a80d5d92a85c94b6b4