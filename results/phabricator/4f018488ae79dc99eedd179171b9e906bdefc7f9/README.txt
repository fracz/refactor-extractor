commit 4f018488ae79dc99eedd179171b9e906bdefc7f9
Author: epriestley <git@epriestley.com>
Date:   Tue Jan 31 12:07:34 2012 -0800

    Restore Lisk transactional methods

    Summary:
    Restores a (simplified and improved) version of Lisk transactions.

    This doesn't actually use transactions anywhere yet. DifferentialRevisionEditor
    is the #1 (and only?) case where we have transaction problems right now, but
    sticking save() inside a transaction unconditionally will leave us holding a
    transaction open for like a million years while we run Herald rules, etc. I want
    to do some refactoring there separately from this diff before making it
    transactional.

    NOTE: @jungejason / @nh, can one of you verify these unit tests pass on
    HPHP/i/vm when you get a chance? I vaguely recall there was some problem with
    (int)$resource. We can land this safely without verifying that, but should check
    before we start using it anywhere.

    Test Plan: Ran unit tests.

    Reviewers: btrahan, nh, jungejason

    Reviewed By: btrahan

    CC: aran, epriestley

    Maniphest Tasks: T605

    Differential Revision: https://secure.phabricator.com/D1515