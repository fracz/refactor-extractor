commit 82f4b263a09630b9937ecb2cc3dea5b21eaa7b8f
Author: Alan Viverette <alanv@google.com>
Date:   Mon Dec 19 15:40:05 2016 -0500

    Add tests, refactor min/targetSdkVersion checks in PackageParser

    Tests were added and verified before refactoring to ensure no behavior
    changes. This is the first step in revising compat checks for O policy.

    Bug: 33455703
    Test: PackageParserTest#testComputeMinSdkVersion
    Test: PackageParserTest#testComputeTargetSdkVersion
    Change-Id: I483b4d23945183702b7e077fa1bd6d3ab5065acc