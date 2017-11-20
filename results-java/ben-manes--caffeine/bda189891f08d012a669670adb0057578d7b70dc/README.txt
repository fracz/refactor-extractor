commit bda189891f08d012a669670adb0057578d7b70dc
Author: Julian Vassev <jvassev@vmware.com>
Date:   Sat Nov 4 10:12:37 2017 +0200

    Make a Node a factory for itself

    This patch refactors the Node to also be a
    factory for self: this saves about 90K as
    there are zero fields & field inits now and half
    as much anonymous classes.

    Jar size is reduce to just under 700K

    NodeFactory is now an iterface with default methods
    and factories are not singletons anymore.