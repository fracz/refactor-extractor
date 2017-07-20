commit 9a8eb4026e4210c8fd42b7662fbde0f9771f09cf
Author: epriestley <git@epriestley.com>
Date:   Thu Jan 8 09:44:02 2015 -0800

    Make race condition window for Conpherence smaller and rarer

    Summary:
    Ref T6713. This isn't very clean, and primarily unblocks D11143.

    After D11143, I have a reliable local race where I submit, get a notification immediately, then get a double update (form submission + notification-triggered update).

    Instead, make the notification updates wait for form submissions.

    This doesn't resolve the race completely. The notification updates don't block chat submission (only the other way around), so if you're really fast you can submit at the same time someone else sends chat and race. But this fixes the most glaring issue.

    The overall structure here is still pretty shaky but I tried to improve things a little, at least.

    Test Plan: Chatted with myself, saw 0 races instead of 100% races.

    Reviewers: btrahan, joshuaspence

    Reviewed By: joshuaspence

    Subscribers: epriestley

    Maniphest Tasks: T6713

    Differential Revision: https://secure.phabricator.com/D11277