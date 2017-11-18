commit 62aeada1203f572d96e305853aea2c89aef20d49
Author: Seigo Nonaka <nona@google.com>
Date:   Fri Aug 7 16:42:22 2015 -0700

    Use TextUtils.substring instead of String.substring.

    This is a small refactoring of using substring method.
    Calling TextUtils.substring is more efficient than calling
    toString and String.substring.

    Change-Id: I0a740b2a2fdbfb6b6155c4e926e17889025082f5