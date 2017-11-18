commit 04a22a26d61866add8a85ca50a623a5d8e4d4bb6
Author: Pavel Maltsev <pavelm@google.com>
Date:   Wed Jan 27 21:56:32 2016 -0800

    Fix indexOutOfBound exception in SystemUI

    Long-pressing events are optional, refactor code a little bit to avoid exceptions.

    Bug:26806128
    Change-Id: Ib883b4d5d31af256fc40ab2b4293ba1bf2abef3f