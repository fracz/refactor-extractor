commit 07b75fe65dcb5b8add8246654c65f95f1191933e
Author: Jason Monk <jmonk@google.com>
Date:   Thu May 14 16:47:03 2015 -0400

    Move NetworkController broadcasts/listeners to BG

    Also do some refactoring to avoid having to sets of callback interfaces
    with 75% of the same data.

    Bug: 19520495
    Change-Id: Ife1c71174c0d6a21f924f7de3cb2f97a04c3d5a1