commit f1c75a63823c5cead8aefd0479a6faa8a7f4dba5
Author: epriestley <git@epriestley.com>
Date:   Thu Aug 29 11:52:29 2013 -0700

    Allow construction of ApplicationSearch queries with GET

    Summary:
    Ref T3775 (discussion here). Ref T2625.

    T3775 presents two problems:

      # Existing tools which linked to `/differential/active/epriestley/` (that is, put a username in the URL) can't generate search links now.
      # Humans can't edit the URL anymore, either.

    I think (1) is an actual issue, and this fixes it. I think (2) is pretty fluff, and this doesn't really try to fix it, although it probably improves it.

    The fix for (1) is:

      - Provide a helper to read a parameter containing either a list of user PHIDs or a list of usernames, so `/?users[]=PHID-USER-xyz` (from a tokenizer) and `/?users=alincoln,htaft` (from an external program) are equivalent inputs.
      - Rename all the form parameters to be more digestable (`authorPHIDs` -> `authors`). Almost all of them were in this form already anyway. This just gives us `?users=alincoln` instead of `userPHIDs=alincoln`.
      - Inside ApplicationSearch, if a request has no query associated with it but does have query parameters, build a query from the request instead of issuing the user's default query. Basically, this means that `/differential/` runs the default query, while `/differential/?users=x` runs a custom query.

    Test Plan: {F56612}

    Reviewers: btrahan

    Reviewed By: btrahan

    CC: aran

    Maniphest Tasks: T2625, T3775

    Differential Revision: https://secure.phabricator.com/D6840