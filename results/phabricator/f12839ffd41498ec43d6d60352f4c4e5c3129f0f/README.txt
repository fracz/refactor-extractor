commit f12839ffd41498ec43d6d60352f4c4e5c3129f0f
Author: Bob Trahan <btrahan@phacility.com>
Date:   Mon Apr 13 12:43:40 2015 -0700

    Conpherenece - improve performance by not fetching data as often from D12347

    Summary: Turns out the pertinent views don't use this data anyway. Additonally, change the remaining pathway to fetch 15% of the rows it fetched in D12347. Fixes T7815.

    Test Plan: clicked around and things worked. my instance always feels snappy though

    Reviewers: epriestley

    Reviewed By: epriestley

    Subscribers: Korvin, epriestley

    Maniphest Tasks: T7815

    Differential Revision: https://secure.phabricator.com/D12399