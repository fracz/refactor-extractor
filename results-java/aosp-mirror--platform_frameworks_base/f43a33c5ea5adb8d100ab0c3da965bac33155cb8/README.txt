commit f43a33c5ea5adb8d100ab0c3da965bac33155cb8
Author: Dianne Hackborn <hackbod@google.com>
Date:   Thu Sep 27 00:48:11 2012 -0700

    Work on issue #7232641: ISE crash when rotating phone in label list mode

    This doesn't fix the problem; I think it is an app problem.  It does
    improve a bunch of the debugging to help better identify what is going
    on, and introduces some checks when adding a fragment to fail
    immediately if we are getting into a state when a fragment is going to
    be in the added list multiple times (which is pretty much guaranteed
    to lead to a failure at some point in the future).

    Change-Id: If3a8700763facd61c4505c6ff872ae66875afc8d