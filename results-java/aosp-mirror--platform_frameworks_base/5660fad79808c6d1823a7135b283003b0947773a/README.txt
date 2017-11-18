commit 5660fad79808c6d1823a7135b283003b0947773a
Author: Yohei Yukawa <yukawa@google.com>
Date:   Wed Jan 27 22:04:09 2016 -0800

    Use LocaleList in KeyboardLayout.

    As an exercise for a new API candidate LocaleList class, this CL does a
    mechanical refactoring to replace Locale[] with LocaleList in
    KeyboardLayout class.  Note that what changed in this CL is just an
    implementation details that is never exposed to application developers.

    One take-away from this exercise is that finding the best-match locale from
    an ordered locale list is really a common pattern.  Perhaps we may want
    to have a guideline for this kind of situation.

    Change-Id: I142379afbaf24d524ff09cf6c7ee7720150f7489