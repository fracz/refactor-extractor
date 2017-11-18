commit 13f00f07d69f087cf186c23ae6c6fc938e2be561
Author: Jeff Brown <jeffbrown@google.com>
Date:   Fri Oct 31 14:45:50 2014 -0700

    Implement multi-press behavior for power key.

    Added support for brightness boost or setting theater
    mode from the power key.  This behavior is disabled by default and
    must be enabled for specific products in a config.xml overlay.

    Because the power key is already so overloaded, refactored the
    code to split out handling of the similar ENDCALL button and
    renamed all of the state that has to do with screenshot chord
    detection to avoid confusion.

    Bug: 17949215
    Change-Id: Id282133188e3781472aabb64fabcee7b98d0c77d