commit a9c16fbe4ee3d3209fda89cfa28663e690660484
Author: epriestley <git@epriestley.com>
Date:   Fri Jan 17 16:09:51 2014 -0800

    Reduce parse latency for changes pushed to hosted repositories

    Summary:
    Currently, we can sit in a sleep() for up to 15 seconds (or longer, in some cases), even if there's a parse request.

    Try polling the DB every second to see if we should wake up instead. This might or might not produce significant improvements, but seems OK to try and see.

    Also a small fix for logging branch names with `%` in them, etc.

    Test Plan: Ran `phd debug pulllocal` and pushed commits, saw them parse within a second.

    Reviewers: btrahan

    Reviewed By: btrahan

    CC: aran, dctrwatson

    Differential Revision: https://secure.phabricator.com/D7998