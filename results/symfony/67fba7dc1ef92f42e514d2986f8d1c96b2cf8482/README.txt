commit 67fba7dc1ef92f42e514d2986f8d1c96b2cf8482
Merge: fef2474 181f256
Author: Fabien Potencier <fabien.potencier@gmail.com>
Date:   Tue Mar 15 17:13:25 2016 +0100

    minor #18139 [FrameworkBundle] Replace kernel.debug with member variable (patrick-mcdougle)

    This PR was merged into the 2.7 branch.

    Discussion
    ----------

    [FrameworkBundle] Replace kernel.debug with member variable

    | Q             | A
    | ------------- | ---
    | Branch        | 2.7
    | Bug fix?      | no
    | New feature?  | no
    | BC breaks?    | unlikely
    | Deprecations? | no
    | Tests pass?   | yes
    | Fixed tickets | n/a
    | License       | MIT
    | Doc PR        | n/a

    While digging around I noticed that someone added debug to the constructor, but didn't replace all the `%kernel.debug%` strings. This PR is just to improve consistency.

    Technically this could cause a BC break if someone was using a compiler pass to alter the argument of the constructor while the other config was relying on `%kernel.debug%`, but the likelihood of that is probably very low.

    Commits
    -------

    181f256 Use debug member variable