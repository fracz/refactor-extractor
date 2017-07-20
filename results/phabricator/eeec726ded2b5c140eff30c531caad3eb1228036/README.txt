commit eeec726ded2b5c140eff30c531caad3eb1228036
Author: epriestley <git@epriestley.com>
Date:   Mon Apr 2 10:41:03 2012 -0700

    Remove all current Authors / Reviewers from "CCWelcome" mail

    Summary:
    We'll incorrectly send CCWelcome mail to users who would be added as CCs but are blocked by the new "$dont_add" stuff, for
    example when a revision is updated and the user has a Herald rule which triggers them getting CC'd. See D2057.

    Potentially a better fix for this would be to have "addCCs" return a list of the CCs it actually added, rather than duplicating the
    logic of removing CCs in two places. However, that's not trivial since it's just a wrapper around alterRelationships() which is nasty
    and would need a more complicated return type. I think this whole thing will get a refactoring pass at some point -- I want to build a
    more generic "associations"-like datastore and replace some of the ad-hoc associations with it. So maybe I can clean it up when that
    happens. For now, this should fix the immediate problem.

    Test Plan: Updated a revision, didn't get CC welcomed.

    Reviewers: vrana, btrahan

    Reviewed By: vrana

    CC: aran

    Differential Revision: https://secure.phabricator.com/D2072