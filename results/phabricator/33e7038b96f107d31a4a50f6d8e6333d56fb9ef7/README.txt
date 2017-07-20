commit 33e7038b96f107d31a4a50f6d8e6333d56fb9ef7
Author: Bob Trahan <btrahan@phacility.com>
Date:   Thu May 7 16:04:56 2015 -0700

    Conpherence - improve performance by handling dropdowns (notifications, threads) as standard response data

    Summary: Ref T7708. Rather than invoking the general client -> server dropdown refresh path, return the data with the various conpherence requests and update the dropdowns that way. Saves 2 client -> server requests per conpherence action.

    Test Plan: loaded up /conpherence/ and noted message count deduct correctly. clicked specific message and noted message count deduct successfully. did same two tests via durable column and again saw message counts deduct successfully.

    Reviewers: epriestley

    Reviewed By: epriestley

    Subscribers: Korvin, epriestley

    Maniphest Tasks: T7708

    Differential Revision: https://secure.phabricator.com/D12761