commit a246c85c6b51faabafcd71a7da7a4c3d89158340
Author: epriestley <git@epriestley.com>
Date:   Tue Mar 25 16:08:40 2014 -0700

    Use ApplicationTransactions and CustomField to implement build steps

    Summary:
    Ref T1049. Fixes T4602. Moves all the funky field stuff to CustomField. Uses ApplicationTransactions to apply and record edits.

    This makes "artifact" fields a little less nice (but still perfectly usable). With D8599, I think they're reasonable overall. We can improve this in the future.

    All other field types are better (e.g., fixes weird bugs with "bool", fixes lots of weird behavior around required fields), and this gives us access to many new field types.

    Test Plan:
    Made a bunch of step edits. Here's an example:

    {F133694}

    Note that:

      - "Required" fields work correctly.
      - the transaction record is shown at the bottom of the page.

    Reviewers: btrahan

    Reviewed By: btrahan

    Subscribers: epriestley

    Maniphest Tasks: T4602, T1049

    Differential Revision: https://secure.phabricator.com/D8600