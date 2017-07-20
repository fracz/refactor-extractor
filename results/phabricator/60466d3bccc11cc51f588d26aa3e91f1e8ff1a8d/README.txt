commit 60466d3bccc11cc51f588d26aa3e91f1e8ff1a8d
Author: Bob Trahan <bob.trahan@gmail.com>
Date:   Wed Oct 24 13:22:24 2012 -0700

    Create a status tool by giving /calendar/ some teeth

    Summary: you can now add, edit, and delete status events. also added a "description" to status events and surface it in the big calendar view on mouse hover. some refactoring changes as well to make validation logic centralized within the storage class.

    Test Plan: added, edited, deleted. yay.

    Reviewers: epriestley, vrana

    Reviewed By: epriestley

    CC: aran, Korvin

    Maniphest Tasks: T407

    Differential Revision: https://secure.phabricator.com/D3810