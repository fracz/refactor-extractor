commit 49cc3d9f0daba90d759fbee2022e24924b443ddb
Author: epriestley <git@epriestley.com>
Date:   Mon Mar 19 19:19:28 2012 -0700

    Simplify and improve the "burnup" chart

    Summary:
      - We incorrectly count resolution changes and other noise as opens / closes.
      - Show one graph: open bugs over time (red line minus green line). This and its derivative are the values you actually care about. It is difficult to see the derivative with both lines, but easy with one line.

    Test Plan: Looked at burnup chart. Saw charty things. Verified resolution changes no longer make the line move.

    Reviewers: btrahan

    Reviewed By: btrahan

    CC: aran, epriestley

    Maniphest Tasks: T923, T982

    Differential Revision: https://secure.phabricator.com/D1945