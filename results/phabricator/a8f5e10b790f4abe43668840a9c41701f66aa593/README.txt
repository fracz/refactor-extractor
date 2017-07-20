commit a8f5e10b790f4abe43668840a9c41701f66aa593
Author: epriestley <git@epriestley.com>
Date:   Tue Jun 30 11:19:58 2015 -0700

    Cache remarkup engines in Feed

    Summary: Ref T8631. This method uses cached settings.

    Test Plan: Saw 40% performance improvement locally (reduced 200ms of 500ms).

    Reviewers: btrahan

    Reviewed By: btrahan

    Subscribers: epriestley

    Maniphest Tasks: T8631

    Differential Revision: https://secure.phabricator.com/D13480