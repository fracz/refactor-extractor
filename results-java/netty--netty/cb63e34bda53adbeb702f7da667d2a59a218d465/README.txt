commit cb63e34bda53adbeb702f7da667d2a59a218d465
Author: nmittler <nathanmittler@google.com>
Date:   Mon Mar 30 09:41:28 2015 -0700

    Removing unnecessary sort in remote flow controller.

    Motivation:

    The DefaultHttp2RemoteFlowController's priority algorithm doesn't really need to sort the children by weight since it already fairly distributes data based on weight.

    Modifications:

    Removing the sorting in the priority algorithm and updating one test to allow a small bit of variability in the results.

    Result:

    Slight improvement on the performance of the priority algorithm.