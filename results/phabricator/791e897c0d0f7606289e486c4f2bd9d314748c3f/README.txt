commit 791e897c0d0f7606289e486c4f2bd9d314748c3f
Author: Bob Trahan <btrahan@phacility.com>
Date:   Thu May 7 11:26:48 2015 -0700

    Conpherence - improve reliability of message delivery

    Summary:
    Ref T7755. T7755#107290 reproduced for me reliably and now it does not. Cleaned up the logic around in flight updates as it was not correct.

    Not sure this is enough to close T7755, but maybe?

    Test Plan: T7755#107290 no longer reproduces!

    Reviewers: epriestley

    Reviewed By: epriestley

    Subscribers: Korvin, epriestley

    Maniphest Tasks: T6713, T7755

    Differential Revision: https://secure.phabricator.com/D12755