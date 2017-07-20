commit a5f8846f472b36e637804fd43a293b6eeda49acb
Author: epriestley <git@epriestley.com>
Date:   Tue Feb 7 14:58:46 2012 -0800

    Use a unique random key to identify queries, not a sequential ID

    Summary:
    We save search information and then redirect to a "/search/<query_id>/" URI in
    order to make search URIs short and bookmarkable, and save query data for
    analysis/improvement of search results.

    Currently, there's a vague object enumeration security issue with using
    sequential IDs to identify searches, where non-admins can see searches other
    users have performed. This isn't really too concerning but we lose nothing by
    using random keys from a large ID space instead.

      - Drop 'authorPHID', which was unused anyway, so searches can not be
    personally identified, even by admins.
      - Identify searches by random hash keys, not sequential IDs.
      - Map old queries' keys to their IDs so we don't break any existing bookmarked
    URIs.

    Test Plan: Ran several searches, got redirected to URIs with random hashes from
    a large ID space rather than sequential integers.

    Reviewers: arice, btrahan

    Reviewed By: arice

    CC: aran, epriestley

    Differential Revision: https://secure.phabricator.com/D1587