commit 9956b5ed72321bc90b19543e3b95d0d10c3cc1e4
Author: Martin Traverso <mtraverso@gmail.com>
Date:   Fri Jan 13 13:16:44 2017 -0800

    Improve performance of array_join

    It now composes the target method handle up front instead of performing
    the conditional logic on every loop iteration. Also, it avoids using
    MethodHandle.invokeWithArguments(), which does not perform well and should
    be avoided if possible (see https://bugs.openjdk.java.net/browse/JDK-8078511)

    The new implementation shows a ~50% improvement for arrays of 10 elements

      before  avgt   60  366.130 ± 5.974  ns/op
      after   avgt   60  189.217 ± 6.739  ns/op