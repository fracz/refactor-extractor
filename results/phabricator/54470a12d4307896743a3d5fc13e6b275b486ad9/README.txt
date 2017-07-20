commit 54470a12d4307896743a3d5fc13e6b275b486ad9
Author: epriestley <git@epriestley.com>
Date:   Fri Nov 25 13:10:33 2016 -0800

    Execute fulltext queries using a subquery instead of by ordering the entire result set

    Summary:
    Ref T6740. Currently, we issue fulltext queries with an "ORDER BY <score>" on the entire result set.

    For very large result sets, this can require MySQL to do a lot of work. However, this work is generally useless: if you search for some common word like "diff" or "internet" or whatever and match 4,000 documents, the chance that we can score whatever thing you were thinking of at the top of the result set is nearly nothing. It's more useful to return quickly, and let the user see that they need to narrow their query to get useful results.

    Instead of doing all that work, let MySQL find up to 1,000 results, then pick the best ones out of those.

    This actual change is a little flimsy, since our index isn't really big enough to suffer indexing issues. However, searching for common terms on my local install (where I have some large repositories imported and indexed) drops from ~40ms to ~10ms.

    My hope is to improve downstream performance for queries like "translatewiki" here, particularly:

    <https://phabricator.wikimedia.org/T143863>

    That query matches about 300 trillion documents but there's a ~0% chance that the one the user wants is at the top. It takes a couple of seconds to execute, for me. Better to return quickly and let the user refine their results.

    I think this will also make some other changes related to stemming easier.

    This also removes the "list users first" ordering on the query, which made performance more complicated and seems irrelevant now that we have the typeahead.

    Test Plan:
      - Searched for some common terms like "code" locally, saw similar results with better performance.
      - Searched for useful queries (e.g., small result set), got identical results.

    Reviewers: chad

    Reviewed By: chad

    Maniphest Tasks: T6740

    Differential Revision: https://secure.phabricator.com/D16944