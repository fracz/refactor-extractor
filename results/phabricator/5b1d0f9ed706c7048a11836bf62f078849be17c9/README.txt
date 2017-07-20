commit 5b1d0f9ed706c7048a11836bf62f078849be17c9
Author: epriestley <git@epriestley.com>
Date:   Fri Apr 3 16:38:27 2015 -0700

    Remove "metamta.precedence-bulk" option (always enable it)

    Summary: Ref T7746. This might possibly improve deliverability. Or might make it worse. Who knows?

    Test Plan: `grep`

    Reviewers: btrahan

    Reviewed By: btrahan

    Subscribers: epriestley

    Maniphest Tasks: T7746

    Differential Revision: https://secure.phabricator.com/D12266