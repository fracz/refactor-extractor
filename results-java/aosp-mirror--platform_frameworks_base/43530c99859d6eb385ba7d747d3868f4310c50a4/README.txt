commit 43530c99859d6eb385ba7d747d3868f4310c50a4
Author: Jorim Jaggi <jjaggi@google.com>
Date:   Thu May 18 01:53:56 2017 +0200

    Fix refactoring typo

    Seems like when we renamed/rearranged the methods we didn't ignore
    mPolicyVisibility..and that's the most important part!

    Fixes a bug where the dock divider isn't animating when unlocking
    the phone.

    Bug: 36200726
    Change-Id: I4c5e9ca7f1b6e7bf21f793e1cf39d92fe77489e8