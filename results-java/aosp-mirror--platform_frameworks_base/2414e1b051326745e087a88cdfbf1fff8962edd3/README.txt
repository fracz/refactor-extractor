commit 2414e1b051326745e087a88cdfbf1fff8962edd3
Author: Chris Craik <ccraik@google.com>
Date:   Mon Dec 12 13:56:15 2016 -0800

    Outline & Path perf improvements

    Bug: 33460152
    Test: device boots, ViewShowHidePerfTests#add[Factory:NestedLinearLayoutTree,depth:6] 770us -> 650us (userdebug 960MHz bullhead)

    Improve perf for outline & simple path methods

    native_methodName -> nMethodName in Path.java

    Change-Id: Id2374bbaca3256d2e4f19dae9abe67f794a171b3