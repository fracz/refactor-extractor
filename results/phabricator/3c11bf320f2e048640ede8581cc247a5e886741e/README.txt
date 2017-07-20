commit 3c11bf320f2e048640ede8581cc247a5e886741e
Author: Bob Trahan <bob.trahan@gmail.com>
Date:   Mon Nov 12 13:28:45 2012 -0800

    improve calendar status editing

    Summary: make it so a given user can click to edit status from calendar view. also fix bug so when editing status existing "type" is selected

    Test Plan: edited status from calendar view and observed "sporadic" status sticky

    Reviewers: epriestley, chad

    Reviewed By: chad

    CC: aran, Korvin

    Maniphest Tasks: T2060, T2061

    Differential Revision: https://secure.phabricator.com/D3957