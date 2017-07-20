commit d9c91f857cefe9167ac668966b60534a55f2e5d5
Author: epriestley <git@epriestley.com>
Date:   Sun Oct 30 08:21:12 2016 -0700

    Apply a TYPE_CREATE transaction when importing events to improve strings in timeline

    Summary: Ref T10747. This turns on the newer EditEngine behavior so we get a nice "X created this event." transaction, instead of an "X renamed this from <nothing> to Event Name."

    Test Plan: Imported an event, saw a nice timeline.

    Reviewers: chad

    Reviewed By: chad

    Maniphest Tasks: T10747

    Differential Revision: https://secure.phabricator.com/D16771