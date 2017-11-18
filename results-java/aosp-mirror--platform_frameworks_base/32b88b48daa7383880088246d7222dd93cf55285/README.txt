commit 32b88b48daa7383880088246d7222dd93cf55285
Author: Neil Fuller <nfuller@google.com>
Date:   Mon Sep 29 17:56:01 2014 +0100

    Replacing FloatMath native implementation with calls to Math

    On modern versions of Android running in AOT mode
    FloatMath is slower than Math. Calls to Math.sqrt(),
    etc. are replaced by intrinsics which can be as small
    as a single CPU opcode.

    When running in interpreted mode the new
    implementation is unfortunately slower, but I'm
    judging this acceptable and likely to be improved
    over time. This change saves a small amount of native
    code.

    Example timings:

    Mako AOSP AOT:

    Method: Original / New / Direct call to Math
    ceil: 596ns / 146.ns / 111ns
    sqrt: 694ns / 56ns / 25ns

    Mako AOSP interpreted:

    Method: Original / New / Direct call to Math
    ceil: 1900ns / 2307ns / 1485ns
    sqrt: 1998ns / 2603ns / 1788ns

    Other calls Mako AOT:

    Method: Original / New
    cos: 635ns / 270ns
    exp: 566ns / 324ns
    floor: 604ns / 150ns
    hypot: 631ns / 232ns
    pow: 936ns / 643ns
    sin: 641ns / 299ns

    The advice to use Math directly, in preference to
    FloatMath, is still good. FloatMath will be deprecated
    separately.

    Bug: https://code.google.com/p/android/issues/detail?id=36199
    Change-Id: I8d1947d88b3c576643138b1df589fb9da7c1ab88