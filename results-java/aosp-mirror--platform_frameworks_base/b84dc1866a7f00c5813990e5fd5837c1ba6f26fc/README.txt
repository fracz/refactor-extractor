commit b84dc1866a7f00c5813990e5fd5837c1ba6f26fc
Author: Adrian Roos <roosa@google.com>
Date:   Fri Dec 2 09:01:09 2016 -0800

    DozeMachine: Improve resilience against out-of-order pulse requests

    Fixes a class of bugs that arise from requesting to pulse at
    inopportune times. Also improves logging such that we know what
    state transition failed to validate.

    Test: runtest -x frameworks/base/packages/SystemUI/tests/src/com/android/systemui/doze/DozeMachineTest.java
    Change-Id: If0bbe003c4805fd180d013dadbc28addc5bb0dd4
    Fixes: 32869355