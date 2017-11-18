commit 68bfe0a37a0dcef52abd81688d8520c5d16e1a85
Author: John Reck <jreck@google.com>
Date:   Tue Jun 24 15:34:58 2014 -0700

    Animator refactoring & fixes

     Tweaks animators to have less unnecessary refcounting

     Pull animator management out into seperate class

     More control to tweak animator lifecycle, such as doing
     Java-side handling of start delay by attaching but not
     starting the animator

    Change-Id: I4ff8207580ca11fb38f45ef0007b406e0097281c