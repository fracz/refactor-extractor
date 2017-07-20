commit 378feb3ffb3fa4123ea3cd80da0c8c43cfb7606c
Author: epriestley <git@epriestley.com>
Date:   Mon Jul 16 19:01:43 2012 -0700

    Centralize rendering of application mail bodies

    Summary: This is a minor quality-of-life improvement to prevent D2968 from being as nasty as it is.

    Test Plan: Ran unit tests; generated Differential, Maniphest and Diffusion emails and verified the bodies looked sensible.

    Reviewers: btrahan, vrana

    Reviewed By: vrana

    CC: aran

    Maniphest Tasks: T931

    Differential Revision: https://secure.phabricator.com/D2986