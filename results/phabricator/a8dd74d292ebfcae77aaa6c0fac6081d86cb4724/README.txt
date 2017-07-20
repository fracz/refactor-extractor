commit a8dd74d292ebfcae77aaa6c0fac6081d86cb4724
Author: epriestley <git@epriestley.com>
Date:   Tue Jan 19 17:18:17 2016 -0800

    Add Herald support for projects

    Summary:
    Ref T10054. Ref T6113. I'm going to remove subscribers from projects to fix the confusion between "watch" and "subscribe".

    Users who have unusual use cases where they legitimately want to know when a project's description is updated or members change can use Herald to follow it.

    This is also useful in general and improves consistency, although I don't have too many use cases for it.

    Test Plan: Wrote a Herald rule, edited a project, saw the rule fire and send me email about the change.

    Reviewers: chad

    Reviewed By: chad

    Maniphest Tasks: T6113, T10054

    Differential Revision: https://secure.phabricator.com/D15061