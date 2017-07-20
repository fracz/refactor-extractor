commit 1ee426e4ac82c7afcf0bb7a33327870f0d7f78a9
Author: epriestley <git@epriestley.com>
Date:   Mon Sep 12 07:31:53 2016 -0700

    Add a specialized cache for storing "has setup ever worked?"

    Summary:
    Ref T11613. In D16503/T11598 I refined the setup flow to improve messaging for early-stage setup issues, but failed to fully untangle things.

    We sometimes still try to access a cache which uses configuration before we build configuration, which causes an error.

    Instead, store "are we in flight / has setup ever worked?" in a separate cache which doesn't use the cache namespace. This stops us from trying to read config before building config.

    Test Plan:
    Hit bad extension error with a fake extension, got a proper setup help page:

    {F1812803}

    Solved the error, reloaded, broke things again, got a "friendly" page:

    {F1812805}

    Reviewers: chad

    Reviewed By: chad

    Maniphest Tasks: T11613

    Differential Revision: https://secure.phabricator.com/D16542