commit 9840c07667bb0363c93fb8459e6c08f7e30d5df1
Author: Jeff Brown <jeffbrown@google.com>
Date:   Tue Nov 11 20:21:21 2014 -0800

    Make Message.setAsynchronous() public.

    There are many cases in real world applications where it is desirable
    to continue processing messages on the Looper even when most other
    messages have been suspended by a synchronization barrier pending
    completion of the next drawing frame on vsync.

    Internally the framework is able to mark certain messages as being
    independent of these higher level synchronization invariants by
    flagging them as asynchronous.

    This change exposes the existing function and improves on the
    documentation so that it is clearer what is meant by asynchronous.

    Bug: 18283959
    Change-Id: I775e4c95938123a364b21a9f2c39019bf37e1afd