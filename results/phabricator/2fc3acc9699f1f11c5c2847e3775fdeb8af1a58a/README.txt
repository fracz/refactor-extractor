commit 2fc3acc9699f1f11c5c2847e3775fdeb8af1a58a
Author: epriestley <git@epriestley.com>
Date:   Tue Sep 27 09:03:55 2011 -0700

    Improve time localization code

    Summary:
      - We throw on a missing date right now, in the DateTime constructor. This can
    happen in reasonable cases and this is display code, so handle it more
    gracefully (see T520).
      - This stuff is a little slow and we sometimes render many hundreds of dates
    per page. I've been seeing it in profiles on and off. Memoize timezones to
    improve performance.
      - Some minor code duplication that would have become less-minor with the
    constructor change, consolidate the logic.
      - Add some unit tests and a little documentation.

    Test Plan:
      - Ran unit tests.
      - Profiled 1,000 calls to phabricator_datetime(), cost dropped from ~49ms to
    ~19ms with addition of memoization. This is still slower than I'd like but I
    don't think there's an easy way to squeeze it down further.

    Reviewers: ajtrichards, jungejason, nh, tuomaspelkonen, aran

    Reviewed By: ajtrichards

    CC: aran, ajtrichards, epriestley

    Differential Revision: 966