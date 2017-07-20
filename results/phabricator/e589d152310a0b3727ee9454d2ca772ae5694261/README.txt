commit e589d152310a0b3727ee9454d2ca772ae5694261
Author: epriestley <git@epriestley.com>
Date:   Thu Oct 1 08:12:51 2015 -0700

    Improve error and exception handling for Drydock resources

    Summary:
    Ref T9252. Currently, error handling behavior isn't great and a lot of errors aren't dealt with properly. Try to improve this by making default behaviors better:

      - Yields, slot lock exceptions, and aggregate or proxy exceptions containing an excpetion of these types turn into yields.
      - All other exceptions are considered permanent failures. They break the resource and

    This feels a little bit "magical" but I want to try to get the default behaviors to align reasonably well with expectations so that blueprints mostly don't need to have a ton of error handling. This will probably need at least some refinement down the road, but it's a reasonable rule for all exception/error conditions we currently have.

    Test Plan: I did a clean build, but haven't vetted this super thoroughly. Next diff will do the same thing to leases, then I'll work on stabilizing this code better.

    Reviewers: chad, hach-que

    Reviewed By: hach-que

    Maniphest Tasks: T9252

    Differential Revision: https://secure.phabricator.com/D14211