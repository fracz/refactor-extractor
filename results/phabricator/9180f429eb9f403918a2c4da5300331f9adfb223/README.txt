commit 9180f429eb9f403918a2c4da5300331f9adfb223
Author: epriestley <git@epriestley.com>
Date:   Wed Jun 1 12:14:39 2016 -0700

    Provide a general-purpose, modular user cache for settings and other similar data

    Summary:
    Ref T4103. Currently, we issue a `SELECT * FROM user_preferences ... WHERE userPHID = ...` on every page to load the viewer's settings.

    There are several other questionable data accesses on every page too, most of which could benefit from improved caching strategies (see T4103#178122).

    This query will soon get more expensive, since it may need to load several objects (e.g., the user's settings and their "role profile" settings). Although we could put that data on the User and do both in one query, it's nicer to put it on the Preferences object ("This inherits from profile X") which means we need to do several queries.

    Rather than paying a greater price, we can cheat this stuff into the existing query where we load the user's session by providing a user cache table and doing some JOIN magic. This lets us issue one query and try to get cache hits on a bunch of caches cheaply (well, we'll be in trouble at the MySQL JOIN limit of 61 tables, but have some headroom).

    For now, just get it working:

      - Add the table.
      - Try to get user settings "for free" when we load the session.
      - If we miss, fill user settings into the cache on-demand.
      - We only use this in one place (DarkConsole) for now. I'll use it more widely in the next diff.

    Test Plan:
      - Loaded page as logged-in user.
      - Loaded page as logged-out user.
      - Examined session query to see cache joins.
      - Changed settings, saw database cache fill.
      - Toggled DarkConsole on and off.

    Reviewers: chad

    Reviewed By: chad

    Maniphest Tasks: T4103

    Differential Revision: https://secure.phabricator.com/D16001