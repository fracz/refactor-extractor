commit 791b37e0d9375789c2930ed170d202ea728390fd
Author: Alan Viverette <alanv@google.com>
Date:   Tue Aug 20 17:34:19 2013 -0700

    Allow recycling of Preferences in com.android.* packages

    Refactors hasSpecifiedLayout to canRecycleLayout for readability's
    sake. All boolean references to the method have been inverted.

    BUG: 10079104
    Change-Id: Ie6beda9f0b837f889a6cc6a80377349e98cc4883