commit a91a3cd22f18f6ea0c9e9f6668928fa5c297639d
Author: Bob Trahan <btrahan@phacility.com>
Date:   Tue Apr 14 12:33:02 2015 -0700

    Conpherence - refactor CSS a bit

    Summary: Follow on to D12410. This kills the general CSS file that once powered application transaction in favor of a specific CSS file for the shared transaction styles for both full view and durable column view. I was able to delete some stuff and identify some shared stuff but there is probably more to go here. Also, rename "phabricator-x" to "conpherence-x" for class names.

    Test Plan: viewed full conpherence and durable column conpherence and things looked nice

    Reviewers: chad, epriestley

    Reviewed By: epriestley

    Subscribers: Korvin, epriestley

    Differential Revision: https://secure.phabricator.com/D12412