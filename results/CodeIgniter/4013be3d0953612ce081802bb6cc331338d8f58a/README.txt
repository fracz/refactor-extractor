commit 4013be3d0953612ce081802bb6cc331338d8f58a
Author: Andrey Andreev <narf@devilix.net>
Date:   Tue Sep 10 19:31:04 2013 +0300

    Add support for UPDATE ... RETURNING statements in PostgreSQL

    An improved version of PR #2629.
    Also removes REPLACE from the regular expression, as it is not supported by PostgreSQL.