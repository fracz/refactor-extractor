commit 112acf11cf518b2716da6b7792b598fbfc4e8274
Author: Jason Ge <jungejason@fb.com>
Date:   Mon Jun 18 11:09:25 2012 -0700

    Enable 'jumping to toc' on diffusion commit page

    Summary:
    Diffusion page is sharing the keyboard shortcuts code with
    Differential page. But since the toc (Changes) panel doesn't have id
    'differential-review-toc', the 'jumping to toc' doesn't work. The fix is
    to add the ID. I don't like adding 'Differential' to the Diffusion page.
    Later we should refactor the code to extract the shared components out of Differential.

    Test Plan:
    verified that 't' worked on the diffusion commit page.

    Reviewers: epriestley, nh

    Reviewed By: epriestley

    CC: hwang, aran, Korvin

    Differential Revision: https://secure.phabricator.com/D2500