commit 2c6081ce3593712f30dacd990a97209c791d6ced
Author: Dianne Hackborn <hackbod@google.com>
Date:   Thu Jul 15 17:44:53 2010 -0700

    Implement native key pre-dispatching to IMEs.

    This significantly re-works the native key dispatching code to
    allow events to be pre-dispatched to the current IME before
    being processed by native code.  It introduces one new public
    API, which must be called after retrieving an event if the app
    wishes for it to be pre-dispatched.

    Currently the native code will only do pre-dispatching of
    system keys, to avoid significant overhead for gaming input.
    This should be improved to be smarted, filtering for only
    keys that the IME is interested in.  Unfortunately IMEs don't
    currently provide this information. :p

    Change-Id: Ic1c7aeec8b348164957f2cd88119eb5bd85c2a9f