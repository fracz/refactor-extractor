commit afd86a04205756da56e383c4dc420d2de1d0600b
Author: epriestley <git@epriestley.com>
Date:   Wed Apr 1 08:39:03 2015 -0700

    Improve rules for embedding files received via email

    Summary:
    Ref T7199. Ref T7712. This improves the file rules for email:

      - Embed visible images as thumbnails.
      - Put all other file types in a nice list.

    This "fixes" an issue caused by the opposite of the problem described in T7712 -- files being dropped if the default ruleset is too restrictive. T7712 is the real solution here, but use a half-measure for now.

    Test Plan:
      - Sent mail with two non-images and two images.
      - Got a nice list of non-images and embeds of images.

    Reviewers: btrahan

    Reviewed By: btrahan

    Subscribers: epriestley

    Maniphest Tasks: T7712, T7199

    Differential Revision: https://secure.phabricator.com/D12235