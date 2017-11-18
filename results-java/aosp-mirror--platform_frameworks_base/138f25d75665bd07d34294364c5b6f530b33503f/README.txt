commit 138f25d75665bd07d34294364c5b6f530b33503f
Author: Jim Miller <jaggies@google.com>
Date:   Wed Sep 25 13:46:58 2013 -0700

    Accessibility improvements in keyguard
    - add accessibility descriptions to camera and search light
    - add new onClick handler to simplify launching search and camera
    - plumb camera launch through KeyguardService interface

    Fixes bug 10914360

    Change-Id: Ic85eda9afadba7381be78b477180f7204030cd17