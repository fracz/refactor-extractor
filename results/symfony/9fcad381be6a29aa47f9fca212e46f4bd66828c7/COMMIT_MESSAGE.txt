commit 9fcad381be6a29aa47f9fca212e46f4bd66828c7
Merge: 7c36f0b 5f4015c
Author: Fabien Potencier <fabien.potencier@gmail.com>
Date:   Fri Jul 3 08:43:12 2015 +0200

    minor #15187 Enhance hhvm test skip message (nicolas-grekas)

    This PR was merged into the 2.3 branch.

    Discussion
    ----------

    Enhance hhvm test skip message

    | Q             | A
    | ------------- | ---
    | Bug fix?      | no
    | New feature?  | no
    | BC breaks?    | no
    | Deprecations? | no
    | Tests pass?   | yes
    | Fixed tickets | -
    | License       | MIT
    | Doc PR        | -

    Reading our blog about hhvm (http://symfony.com/blog/symfony-2-3-achieves-100-hhvm-compatibility)
    I'd like to improve the only single test skip in our code base that exists for hhvm.

    HHVM does not trigger strict notices. This is a missing feature of the HHVM parser (https://github.com/facebook/hhvm/issues/5583). But this does not affect in any way the behavior of any PHP app. Strict notices are only for the dev stage.

    There is some FUD-friendliness in the article and in the comments (no offence @javiereguiluz ☮):
    100% hhvm compat is 100% honest. We did not skip any single tests because of errors, issues nor bugs. And the single missing "feature" of hhvm is this one: strict notices. Kudos to hhvm for what they did: parity is close enough so that the delta can be handled in exactly the same way as we handle deltas between PHP versions.

    Commits
    -------

    5f4015c Enhance hhvm test skip message