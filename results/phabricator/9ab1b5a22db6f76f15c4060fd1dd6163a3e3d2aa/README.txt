commit 9ab1b5a22db6f76f15c4060fd1dd6163a3e3d2aa
Author: epriestley <git@epriestley.com>
Date:   Wed Jan 6 17:59:06 2016 -0800

    Make mundane performance improvements to Diffusion browse views

    Summary:
    Ref T2450. This reorganizes code to improve performance.

    Mostly, there are a lot of things which are unique per commit (author name, links, short name, etc), but we were rendering them for every line.

    This often meant we'd render the same author's name thousands of times. This is slower than rendering it only once.

    In 99% of interfaces this doesn't matter, but blame is weird and it's significant on big files.

    Test Plan:
    Locally, `__phutil_library_map__.php` now has costs of roughly:

      - 550ms for main content (from 650ms before the patch).
      - 1,500ms for blame content (frrom 1,800ms before the patch).

    So this isn't huge, is a decent ~20%-ish performance gain for shuffling some stuff around.

    Reviewers: chad

    Reviewed By: chad

    Maniphest Tasks: T2450

    Differential Revision: https://secure.phabricator.com/D14963