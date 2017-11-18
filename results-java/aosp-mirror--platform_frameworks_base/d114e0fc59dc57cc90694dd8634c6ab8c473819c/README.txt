commit d114e0fc59dc57cc90694dd8634c6ab8c473819c
Author: Evan Rosky <erosky@google.com>
Date:   Thu Mar 23 11:20:04 2017 -0700

    Improve rect-level focus ordering

    Previously views were ordered strictly by their tops. This
    lead to many cases of tab-focus moving "backwards". For example
    a horizontal row of views with different heights would not
    always move start-to-end.

    Bug: 34854951
    Bug: 33848452
    Test: Run against UX-provided localized focus-orders to make sure
          it improves behavior. Added a sanity-check CTS test for
          well-behaved (simple) layouts.

    Change-Id: I5b01a301e0bbcbcad472ffdb26ebf4fbb6380756