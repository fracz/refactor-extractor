commit 87d360bb1b4ad1ecf53bd431b47437349288a7a2
Author: Bob Trahan <btrahan@phacility.com>
Date:   Tue Apr 14 12:25:35 2015 -0700

    Conpherence - refactor display classes a bit

    Summary:
    D12409 made me realize this was a bit janky. `PhabricatorTransactionView` was only being used by Conpherence, so move and rename that class to `ConpherenceTransactionView`. Also, rename the existing `ConpherenceTransactionView` to `ConpherenceTransactionRenderer`, moving the actual view bits into the new `ConpherenceTransactionView`. Resulting code is a bit cleaner IMO.

    Diff 1 of 2 (second diff has to be written. =D). Diff 2 will take care of the CSS and possibly clean things up further.

    Test Plan: played around in conpherence full and conpherence column and things looked nice

    Reviewers: epriestley

    Reviewed By: epriestley

    Subscribers: Korvin, epriestley

    Differential Revision: https://secure.phabricator.com/D12410