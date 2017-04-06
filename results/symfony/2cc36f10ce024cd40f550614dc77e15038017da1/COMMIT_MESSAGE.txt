commit 2cc36f10ce024cd40f550614dc77e15038017da1
Merge: fbc9082 b030624
Author: Fabien Potencier <fabien.potencier@gmail.com>
Date:   Fri Aug 2 15:39:24 2013 +0200

    merged branch lmammino/improved-image-validator (PR #8490)

    This PR was squashed before being merged into the master branch (closes #8490).

    Discussion
    ----------

    [Validator] improved image validator

    | Q             | A
    | ------------- | ---
    | Bug fix?      | no
    | New feature?  | yes
    | BC breaks?    | no
    | Deprecations? | no
    | Tests pass?   | yes
    | Fixed tickets | -
    | License       | MIT
    | Doc PR        | symfony/symfony-docs#2840

    CHANGELOG
    * Added options to validate image aspect ratio (`minRatio` and `maxRatio`)
    * Added options to validate if the image ratio is square, landscape or portrait (`allowSquare`, `allowLandscape`, and `allowPortrait`)

    Commits
    -------

    b030624 [Validator] improved image validator