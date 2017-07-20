commit 25cc90d63247c96c0563600f559fb536f8c5dde8
Author: epriestley <git@epriestley.com>
Date:   Tue Jun 28 16:05:05 2016 -0700

    Inch toward using ApplicationSearch to power related objects

    Summary:
    Ref T4788. Fixes T9232. This moves the "search for stuff to attach to this object" flow away from hard-coding and legacy constants and toward something more modular and flexible.

    It also adds an "Edit Commits..." action to Maniphest, resolving T9232. The behavior of the search for commits isn't great right now, but it will improve once these use real ApplicationSearch.

    Test Plan: Edited a tasks' related commits, mocks, tasks, etc.

    Reviewers: chad

    Reviewed By: chad

    Maniphest Tasks: T4788, T9232

    Differential Revision: https://secure.phabricator.com/D16189