commit 0c063801f91f10891339ec9ce5127e26f9f00931
Author: cpovirk <cpovirk@google.com>
Date:   Tue Feb 23 14:02:32 2016 -0800

    Fix 6-year-old test bug.

    The bug is:

      try {
        somethingThatShouldFail();
        fail();
      } catch (AssertionFailedError expected) {
      }

    The fail() is masked by the catch() block.
    I've requested an enhancement to Error Prone to catch this: https://github.com/google/error-prone/issues/393

    Additionally, I improved IteratorTester under GWT: IteratorTester couldn't catch wrong-exception bugs there because of GWT's lack of reflection. This was a known, documented limitation, but the tests still attempted to verify its ability to do so. When I fixed the test bug, they broke. I then realized that I could just fix IteratorTester to work under GWT. It's probably even a simplification.

    Tested:
    global TAP
    []
    -------------
    Created by MOE: https://github.com/google/moe
    MOE_MIGRATED_REVID=115380685