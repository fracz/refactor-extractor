commit 80a7ac10634dabb39644004f3edfc648a2c036f7
Author: Dianne Hackborn <hackbod@google.com>
Date:   Thu Sep 22 18:32:52 2011 -0700

    Fix issue #5321282: Force Stop Button in Battery Screen Not disabled correctly

    If the app had activities still finishing, when we checked whether it was
    now stopped we would get told no.  Also some other improvements:

    - Schedule an idle as part of the force stop, to get any finishing
      activities out of the stack soon rather than waiting for some activity
      to idle.
    - Don't filter out stopped system apps.  This is dangerous because
      system apps may have no way for the user to explicitly launch them,
      so they could get put into a stopped state for which there is no way
      to get them out.  Also if the user really wants a system app to not
      run, the new disabling mechanism is more appropriate.

    Change-Id: I34003f21dac29e2ca0f66a23b88c710de41bab99