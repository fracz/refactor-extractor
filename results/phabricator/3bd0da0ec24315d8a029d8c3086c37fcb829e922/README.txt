commit 3bd0da0ec24315d8a029d8c3086c37fcb829e922
Author: epriestley <git@epriestley.com>
Date:   Fri Aug 19 11:29:28 2016 -0700

    Add a missing table key to improve performance of "Recently Completed Tasks" query

    Summary:
    Fixes T11490. Currently, this query can not use a key and the table size may be quite large.

    Adjust the query so it can use a key for both selection and ordering, and add that key.

    Test Plan: Ran `EXPLAIN` on the old query in production, then added the key and ran `EXPLAIN` on the new query. Saw key in use, and "rows" examined drop from 29,273 to 15.

    Reviewers: chad

    Reviewed By: chad

    Maniphest Tasks: T11490

    Differential Revision: https://secure.phabricator.com/D16423