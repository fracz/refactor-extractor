commit 4369e7d0b087d777e5012e2706acc5be9be47de7
Author: Adam Powell <adamp@google.com>
Date:   Sat May 17 14:16:08 2014 -0700

    Action bar refactoring, round 1

    Decouple PhoneWindow and ActionBarView to allow for using Toolbar in
    some circumstances later.

    Change-Id: I907743e06c3a1203e21cfd84860a1884c66f3527