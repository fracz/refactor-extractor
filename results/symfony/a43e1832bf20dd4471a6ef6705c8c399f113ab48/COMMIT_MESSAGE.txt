commit a43e1832bf20dd4471a6ef6705c8c399f113ab48
Merge: 0a6d3c6 bf71776
Author: Nicolas Grekas <nicolas.grekas@gmail.com>
Date:   Fri Feb 3 13:07:20 2017 +0100

    minor #21515 [FrameworkBundle][Console] JsonDescriptor: Respect original output (ogizanagi)

    This PR was merged into the 2.8 branch.

    Discussion
    ----------

    [FrameworkBundle][Console] JsonDescriptor: Respect original output

    | Q             | A
    | ------------- | ---
    | Branch?       | 2.8
    | Bug fix?      | no
    | New feature?  | no
    | BC breaks?    | no
    | Deprecations? | no
    | Tests pass?   | no
    | Fixed tickets | N/A
    | License       | MIT
    | Doc PR        | N/A

    Follows up #21501.

    This one fixes the keys order (preserved from the order those keys are added from the `JsonDescriptor`), as for the previous mentioned PR, in order to slightly improve the situation when updating the descriptors fixtures.

    @nicolas-grekas : Thanks for taking care of the previous one. There are two other PRs required to me in order to fix everything on every branches, but I wonder if it wouldn't be easier (and reduce noise) to apply the following patches while merging this in upper branches instead:

    - 3.2: https://github.com/ogizanagi/symfony/commit/51a0a1c25d5d1c050d1e38450bc694029055e499
    - master: https://github.com/ogizanagi/symfony/commit/b35a244249c6b82badae7ad626b896f072bcf48d

    WDYT?

    Commits
    -------

    bf71776 [FrameworkBundle][Console] JsonDescriptor: Respect original output