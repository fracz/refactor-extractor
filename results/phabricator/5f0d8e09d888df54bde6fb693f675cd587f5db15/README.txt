commit 5f0d8e09d888df54bde6fb693f675cd587f5db15
Author: epriestley <git@epriestley.com>
Date:   Mon Apr 16 15:55:16 2012 -0700

    Use "user-select: none" to provide a visual cue about copy/paste JS magic

    Summary:
      - For line numbers, use "user-select: none" to make them unselectable. This provides a stronger visual cue that copy/paste is enchanted.
      - In Paste, make it look sensible again after the blame-on-blame refactor in Diffusion. See also TODO to share this code formally.
      - In Diffusion, use the "phabricator-oncopy" behavior.

    NOTE: I left blame/commit columns selectable in Diffusion, since you might reasonably want to copy/paste them?

    NOTE: In Differential, the left side of the diff still highlights, even though it will be copied only if you select part of a line on the left and nothing else. But this seemed like a reasonable behavior, so I left it.

    Test Plan:
      - Looked at Paste. Saw a nice line number column. Selected text, got the expected selection. Copied text, got the expected copy.
      - Looked at Diffusion. Saw a nice line number column, still. Selected text, got expected selection. Copied text, got expected copy.
      - Looked at Differential. Highlighted stuff, got expected results. Copied stuff, got expected results.

    Reviewers: btrahan, vrana, jungejason

    Reviewed By: vrana

    CC: aran

    Maniphest Tasks: T1123

    Differential Revision: https://secure.phabricator.com/D2242