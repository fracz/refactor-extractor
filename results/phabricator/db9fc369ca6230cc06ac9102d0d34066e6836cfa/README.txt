commit db9fc369ca6230cc06ac9102d0d34066e6836cfa
Author: Bob Trahan <btrahan@phacility.com>
Date:   Tue Jun 16 15:33:53 2015 -0700

    Phriction - improve workflow for when user entered slug needs to be normalized.

    Summary: Encountered this playing with T8402 on my test instance. I think warning the user about adding a trailing "/" is unnecessary so don't do it. I think its confusing to not call out spaces / to lump them in with special characters so call out the sapces.

    Test Plan: moved a page around and verified no warning if the slug is missing a "/" as user specified and that a change to spaces is called out

    Reviewers: epriestley

    Reviewed By: epriestley

    Subscribers: epriestley, Korvin

    Differential Revision: https://secure.phabricator.com/D13316