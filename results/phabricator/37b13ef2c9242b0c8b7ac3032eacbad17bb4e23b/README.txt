commit 37b13ef2c9242b0c8b7ac3032eacbad17bb4e23b
Author: epriestley <git@epriestley.com>
Date:   Tue Jul 9 16:23:54 2013 -0700

    Improve UI for selecting profile pictures

    Summary:
    Ref T1703. Move profile pictures to a separate, dedicated interface. Instead of the 35 controls we currently provide, just show all the possible images we can find and then let the user upload an additional one if they want.

    Possible improvements to this interface:

      - Write an edge so we can show old profile pictures too.
      - The cropping/scaling got a bit buggy at some point, fix that.
      - Refresh OAuth sources which we're capable of refreshing before showing images (more work than I really want to deal with).
      - We could show little inset icons for the image source ("f" for Facebook, etc.) instead of just the tooltips.

    Test Plan:
    Chose images, uploaded new images, hit various error cases.

    {F49344}

    Reviewers: chad, btrahan

    Reviewed By: btrahan

    CC: aran

    Maniphest Tasks: T2919, T1703

    Differential Revision: https://secure.phabricator.com/D6398