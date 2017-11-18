commit f9261d20b3773f9785d98f32f62ccfedefba8083
Author: Jeff Brown <jeffbrown@google.com>
Date:   Fri Feb 3 13:49:15 2012 -0800

    Process input events immediately when received.

    Reduce the latency for handling input events by ensuring that they are
    handled as soon as they are received from the dispatcher.  This also
    makes it easier for the input system to intelligently batch motion events.

    This is part of a series of changes to improve input system pipelining.

    Bug: 5963420
    Change-Id: I0b3f6cbb3de5ac3c4eed35dfc774d6bd4a0b07fc