commit 0513a242352ee00f0486d93ab7c10c9708741885
Author: epriestley <git@epriestley.com>
Date:   Wed Jan 18 05:57:03 2017 -0800

    Fix a bad constant in "audit.query"

    Summary: Fixes T12117. I typed or copy/pasted this constant wrong while refactoring during T10978.

    Test Plan: Called `audit.query`.

    Reviewers: chad

    Reviewed By: chad

    Maniphest Tasks: T12117

    Differential Revision: https://secure.phabricator.com/D17218