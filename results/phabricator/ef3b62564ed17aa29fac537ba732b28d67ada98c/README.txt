commit ef3b62564ed17aa29fac537ba732b28d67ada98c
Author: Bob Trahan <btrahan@phacility.com>
Date:   Thu May 7 16:01:41 2015 -0700

    Conpherence - improve durable column performance when sending updates

    Summary: Ref T7708. We were generating things like the files widget when users sent a comment. This is unnecessary if we are in minimal display mode. This saves us fetching some data + rendering.

    Test Plan: sent messages successfully in durable column and full conpherence view

    Reviewers: epriestley

    Reviewed By: epriestley

    Subscribers: Korvin, epriestley

    Maniphest Tasks: T7708

    Differential Revision: https://secure.phabricator.com/D12760