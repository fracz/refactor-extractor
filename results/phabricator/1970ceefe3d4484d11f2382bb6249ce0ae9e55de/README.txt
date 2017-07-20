commit 1970ceefe3d4484d11f2382bb6249ce0ae9e55de
Author: vrana <jakubv@fb.com>
Date:   Tue Jul 24 17:58:33 2012 -0700

    Compute reviewer stats

    Summary:
    The final goal is to display reviewers response time on homepage.
    This is a building block for it.

    The algorithm is quite strict - it doesn't count simple comment as response because reviewers would be able to cheat with comments like "I'm overwhelmed right now and will review next week".
    We are more liberate in Phabricator where reviewers response with comments without changing the status quite often but I'm not trying to improve response times in Phabricator so this is irrelevant.
    Reviewers in Facebook changes status more often (to clean their queue) so I follow this approach.

    There is currently no way to track reviewers silently added and removed in Edit Revision but it's not a big deal.
    The algorithm doesn't track commandeered revision, there's a TODO for it.

    Response times are put in two buckets: `$reviewed` and `$not_reviewed`.
    `$reviewed` contains reviewers who took action, `$not_reviewed` contains reviewers who didn't respond on time.
    I will probably compute average time from `$reviewed` and raise it for those `$not_reviewed` that are higher than this average.
    The idea is to not favor reviewers who were only lucky for being in a group with someone fast.

    Test Plan: Passed test.

    Reviewers: epriestley

    Reviewed By: epriestley

    CC: aran, Korvin

    Differential Revision: https://secure.phabricator.com/D3062