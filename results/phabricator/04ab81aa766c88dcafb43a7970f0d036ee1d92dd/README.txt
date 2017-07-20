commit 04ab81aa766c88dcafb43a7970f0d036ee1d92dd
Author: epriestley <git@epriestley.com>
Date:   Fri Jan 23 18:44:23 2015 -0800

    Use "webkit-overflow-scrolling" on main page scroll view

    Summary: This seems to improve behavior on iOS, good call.

    Test Plan: Hard to be totally sure since my local install isn't set up with a real phone, but behavior seems better on iOS simulator.

    Reviewers: chad

    Reviewed By: chad

    Subscribers: epriestley

    Differential Revision: https://secure.phabricator.com/D11481