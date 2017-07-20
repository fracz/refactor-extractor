commit 2b0b9a15734ca7b885fd2fe249c7f65a16d72cba
Author: epriestley <git@epriestley.com>
Date:   Mon Jul 9 15:20:56 2012 -0700

    Add a generic multistep Markup cache

    Summary:
    The immediate issue this addresses is T1366, adding a rendering cache to Phriction. For wiki pages with code blocks especially, rerendering them each time is expensive.

    The broader issue is that out markup caches aren't very good right now. They have three major problems:

    **Problem 1: the data is stored in the wrong place.** We currently store remarkup caches on objects. This means we're always loading it and passing it around even when we don't need it, can't genericize cache management code (e.g., have one simple script to drop/GC caches), need to update authoritative rows to clear caches, and can't genericize rendering code since each object is different.

    To solve this, I created a dedicated cache database that I plan to move all markup caches to use.

    **Problem 2: time-variant rules break when cached.** Some rules like `**bold**` are time-invariant and always produce the same output, but some rules like `{Tnnn}` and `@username` are variant and may render differently (because a task was closed or a user is on vacation). Currently, we cache the raw output, so these time-variant rules get locked at whatever values they had when they were first rendered. This is the main reason Phriction doesn't have a cache right now -- I wanted `{Tnnn}` rules to reflect open/closed tasks.

    To solve this, I split markup into a "preprocessing" phase (which does all the parsing and evaluates all time-invariant rules) and a "postprocessing" phase (which evaluates time-variant rules only). The preprocessing phase is most of the expense (and, notably, includes syntax highlighting) so this is nearly as good as caching the final output. I did most of the work here in D737 / D738, but we never moved to use it in Phabricator -- we currently just do the two operations serially in all cases.

    This diff splits them apart and caches the output of preprocessing only, so we benefit from caching but also get accurate time-variant rendering.

    **Problem 3: cache access isn't batched/pipelined optimally.** When we're rendering a list of markup blocks, we should be able to batch datafetching better than we do. D738 helped with this (fetching is batched within a single hunk of markup) and this improves batching on cache access. We could still do better here, but this is at least a step forward.

    Also fixes a bug with generating a link in the Phriction history interface ($uri gets clobbered).

    I'm using PHP serialization instead of JSON serialization because Remarkup does some stuff with non-ascii characters that might not survive JSON.

    Test Plan:
      - Created a Phriction document and verified that previews don't go to cache (no rows appear in the cache table).
      - Verified that published documents come out of cache.
      - Verified that caches generate/regenerate correctly, time-variant rules render properly and old documents hit the right caches.

    Reviewers: btrahan

    Reviewed By: btrahan

    CC: aran

    Maniphest Tasks: T1366

    Differential Revision: https://secure.phabricator.com/D2945