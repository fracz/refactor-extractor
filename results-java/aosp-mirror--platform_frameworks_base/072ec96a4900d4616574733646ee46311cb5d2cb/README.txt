commit 072ec96a4900d4616574733646ee46311cb5d2cb
Author: Jeff Brown <jeffbrown@google.com>
Date:   Tue Feb 7 14:46:57 2012 -0800

    Implement batching of input events on the consumer side.

    To support this feature, the input dispatcher now allows input
    events to be acknowledged out-of-order.  As a result, the
    consumer can choose to defer handling an input event from one
    device (because it is building a big batch) while continuing
    to handle input events from other devices.

    The InputEventReceiver now sends a notification when a batch
    is pending.  The ViewRoot handles this notification by scheduling
    a draw on the next sync.  When the draw happens, the InputEventReceiver
    is instructed to consume all pending batched input events, the
    input event queue is fully processed (as much as possible),
    and then the ViewRoot performs traversals as usual.

    With these changes in place, the input dispatch latency is
    consistently less than one frame as long as the application itself
    isn't stalled.  Input events are delivered to the application
    as soon as possible and are handled as soon as possible.  In practice,
    it is no longer possible for an application to build up a huge
    backlog of touch events.

    This is part of a series of changes to improve input system pipelining.

    Bug: 5963420

    Change-Id: I42c01117eca78f12d66d49a736c1c122346ccd1d