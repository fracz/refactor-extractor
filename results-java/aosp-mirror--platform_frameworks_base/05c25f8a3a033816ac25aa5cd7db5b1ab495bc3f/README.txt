commit 05c25f8a3a033816ac25aa5cd7db5b1ab495bc3f
Author: Yohei Yukawa <yukawa@google.com>
Date:   Mon Feb 22 12:41:17 2016 -0800

    Unify windowGainedFocus() and startInput().

    This is a safe refactoring that changes nothing.

    In order to improve the keyboard dismissal lags [1][2], we have used
    IMMS#windowGainedFocus() as a combined event to do startInput() in
    certain situations.

    To make the intent of those CLs clear, this CL renames
    IMMS#windowGainedFocus() to IMMS#startInputOrWindowGainedFocus().  Note
    that these are @hide internal IPC protocols.  Hence this change is never
    observable to application developers.

     [1] I8494cbd6e19e2ab6db03f2463d9906680dda058b
         a82ba54b0bbc3ff41f29db3998806cb45b261d58
     [2] Icb58bef75ef4bf9979f3e2ba88cea20db2e2c3fb
         7663d80f6b6fd6ca7a736c3802013a09c0abdeb9

    Bug: 25373872
    Change-Id: I56934f18e30d90fcdf77bcbb0c35a92a5feb1b82