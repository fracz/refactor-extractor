commit 46b9ac0ae2162309774a7478cd9d4e578747bfc2
Author: Jeff Brown <jeffbrown@google.com>
Date:   Thu Apr 22 18:58:52 2010 -0700

    Native input dispatch rewrite work in progress.

    The old dispatch mechanism has been left in place and continues to
    be used by default for now.  To enable native input dispatch,
    edit the ENABLE_NATIVE_DISPATCH constant in WindowManagerPolicy.

    Includes part of the new input event NDK API.  Some details TBD.

    To wire up input dispatch, as the ViewRoot adds a window to the
    window session it receives an InputChannel object as an output
    argument.  The InputChannel encapsulates the file descriptors for a
    shared memory region and two pipe end-points.  The ViewRoot then
    provides the InputChannel to the InputQueue.  Behind the
    scenes, InputQueue simply attaches handlers to the native PollLoop object
    that underlies the MessageQueue.  This way MessageQueue doesn't need
    to know anything about input dispatch per-se, it just exposes (in native
    code) a PollLoop that other components can use to monitor file descriptor
    state changes.

    There can be zero or more targets for any given input event.  Each
    input target is specified by its input channel and some parameters
    including flags, an X/Y coordinate offset, and the dispatch timeout.
    An input target can request either synchronous dispatch (for foreground apps)
    or asynchronous dispatch (fire-and-forget for wallpapers and "outside"
    targets).  Currently, finding the appropriate input targets for an event
    requires a call back into the WindowManagerServer from native code.
    In the future this will be refactored to avoid most of these callbacks
    except as required to handle pending focus transitions.

    End-to-end event dispatch mostly works!

    To do: event injection, rate limiting, ANRs, testing, optimization, etc.

    Change-Id: I8c36b2b9e0a2d27392040ecda0f51b636456de25