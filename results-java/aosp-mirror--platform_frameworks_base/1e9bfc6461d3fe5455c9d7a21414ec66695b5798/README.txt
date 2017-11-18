commit 1e9bfc6461d3fe5455c9d7a21414ec66695b5798
Author: Tyler Gunn <tgunn@google.com>
Date:   Wed Aug 19 11:18:58 2015 -0700

    Fix incorrect android.telecom.Call.Details equality check.

    The the android.telecom.Call.Details class provides its own equals
    implementation.  Recently added in M is to also check if the mExtras
    and mIntentExtras are different.  Unfortunately, Bundles do not implement
    equals.  As a result when Telecom calls are parceled and sent to the
    InCallServices, this means that the internalUpdate method will always
    assume that the Details of a call have changed, even if they have not.
    This was causing a LOT of extra calls to onUpdate in the InCall UI (2x the
    amount).  Although there is still room for improvement in the number of
    callbacks from Telecom, this fix prevents a pretty significant regression
    on that front.

    Bug: 23218195
    Change-Id: I128e996faf60376ed3df1dc848a97c4a7b0482ee