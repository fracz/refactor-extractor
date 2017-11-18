commit 99104836083738f9e5e73ab4170879d91de6ba67
Author: Selim Cinek <cinek@google.com>
Date:   Wed Jan 25 14:47:33 2017 -0800

    Further improved the coloring optimization

    Fixes a few follow up bugs from the colorized
    notification CL.

    This also allows media notifications with a media
    session to be colorized by default.

    Test: existing tests pass
    Bug: 34469375
    Change-Id: I36348591a84fcd39d103d0ae3f64720f2fca2cf2