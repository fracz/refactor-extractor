commit 857e3aee83def1166646b3cf14c4b37f99fbaa7f
Author: epriestley <git@epriestley.com>
Date:   Wed Mar 5 10:44:21 2014 -0800

    Improve ApplicationTransaction behavior for poorly constructed transactions

    Summary:
    Ref T2222. Five very small improvements:

      - I hit this exception and it took a bit to understand which transaction was causing problems. Add an `Exception` subclass which does a better job of making the message debuggable.
      - The `oldValue` of a transaction may be `null`, legitimately (for example, changing the `repositoryPHID` for a revision from `null` to some valid PHID). Do a check to see if `setOldValue()` has been called, instead of a check for a `null` value.
      - Add an additional check for the other case (shouldn't have a value, but does).
      - When we're not generating a value, don't bother calling the code to generate it. The best case scenario is that it has no effect; any effect it might have (changing the value) is always wrong.
      - Maniphest didn't fall back to the parent correctly when computing this flag, so it got the wrong result for `CustomField` transactions.

    Test Plan: Resolved the issue I was hitting more easily, made updates to a `null`-valued custom field, and applied other normal sorts of transactions successfully.

    Reviewers: btrahan

    Reviewed By: btrahan

    CC: aran

    Maniphest Tasks: T4557, T2222

    Differential Revision: https://secure.phabricator.com/D8401