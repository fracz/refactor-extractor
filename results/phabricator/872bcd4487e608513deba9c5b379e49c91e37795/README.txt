commit 872bcd4487e608513deba9c5b379e49c91e37795
Author: epriestley <git@epriestley.com>
Date:   Wed Jul 13 09:29:02 2016 -0700

    Make limits and ranges work better with Calendar event queries

    Summary:
    Fixes T8911. This corrects several issues which could crop up if a calendar event query matched more results than the query limit:

      - The desired order was not applied by the SearchEngine -- it applies the first builtin order instead. Provide a proper builtin order.
      - When we generate ghosts, we can't do limiting in the database because we may select and then immediately discard a large number of parent events which are outside of the query range.
        - For now, just don't limit results to get the behavior correct.
        - This may need to be refined eventually to improve performance.
      - When trimming events, we could trim parents and fail to generate ghosts from them. Separate parent events out first.
      - Try to simplify some logic.

    Test Plan: An "Upcoming" dashboard panel with limit 10 and the main Calendar "Upcoming Events" UI now show the same results.

    Reviewers: chad

    Reviewed By: chad

    Maniphest Tasks: T8911

    Differential Revision: https://secure.phabricator.com/D16289