commit d08576ed4e9946387349193d7e194b02b5e23763
Author: Bob Trahan <bob.trahan@gmail.com>
Date:   Wed Mar 5 18:40:28 2014 -0800

    Workboards - add task create + improve task placement wrt priority edits

    Summary: Fixes T4553, T4407.

    Test Plan: created tasks and they showed up in the proper column. edited task priority and they moved about sensically.

    Reviewers: chad, epriestley

    Reviewed By: epriestley

    CC: Korvin, epriestley, aran

    Maniphest Tasks: T4553, T4407

    Differential Revision: https://secure.phabricator.com/D8420