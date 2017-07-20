commit 4dd5bcf1cdc38ade60c3316232c9040223ccd301
Author: epriestley <git@epriestley.com>
Date:   Thu Jul 5 06:12:35 2012 -0700

    Don't fail in Diffusion if .gitmodules is missing

    Summary:
    See T1448. If this file isn't present, just move on instead of failing, since it's a (sort of) legitimate repository state.

    Also fix some silliness a little later that got introduced in refactoring, I think.

    Test Plan: Added an external to my test repo and removed ".gitmodules". Verified that the directory is now viewable after this patch.

    Reviewers: btrahan, davidreuss, jungejason

    Reviewed By: davidreuss

    CC: aran

    Maniphest Tasks: T1448

    Differential Revision: https://secure.phabricator.com/D2922