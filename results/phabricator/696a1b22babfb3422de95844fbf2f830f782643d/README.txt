commit 696a1b22babfb3422de95844fbf2f830f782643d
Author: epriestley <git@epriestley.com>
Date:   Tue Oct 23 12:01:59 2012 -0700

    Make feed stories properly respect object policies

    Summary:
      - When a feed story's primary object is a Policy object, use its visibility policy to control story visibility. Leave an exception for
      - Augment PhabricatorPolicyAwareQuery so queries may do pre-policy filtering without the need to handle their own buffering/cursor code. (We could slightly improve this: if a query returns less than a page of pre-filtered results we could keep getting pre-filtered results until we had at least a page's worth and then filter them all at once.)
      - Load and attach "required objects" to feed stories. We need this for policies anyway, and it will let us simplify story implementations by sourcing data directly from the object when we don't have some need to denormalize it (e.g., "title was changed from X to Y" needs to save the values of X and Y from when we published the story, but "user asked question X" can reflect the current version of the question).

    Test Plan: Loaded main feed, project feed, notification menu / dropdown, notificaiton list, paginated things.

    Reviewers: btrahan, vrana

    Reviewed By: btrahan

    CC: aran

    Differential Revision: https://secure.phabricator.com/D3783