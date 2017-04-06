commit 971e6e30b44ceb14108fee457cd9a68186fe8ad4
Merge: 1cd63e7 fea18aa
Author: Nicolas Grekas <nicolas.grekas@gmail.com>
Date:   Thu Dec 8 16:02:43 2016 +0100

    bug #20530 [Serializer] Remove AbstractObjectNormalizer::isAttributeToNormalize (dunglas)

    This PR was squashed before being merged into the 3.1 branch (closes #20530).

    Discussion
    ----------

    [Serializer] Remove AbstractObjectNormalizer::isAttributeToNormalize

    | Q             | A
    | ------------- | ---
    | Branch?       | 3.1
    | Bug fix?      | yes
    | New feature?  | no
    | BC breaks?    | unclear
    | Deprecations? | no
    | Tests pass?   | yes
    | Fixed tickets | n/a
    | License       | MIT
    | Doc PR        | n/a

    I've introduced this method by error in #17113. It has been forgotten during a refactoring. It has always been unused, is not covered by our test suite and has never been documented.

    Technically it's a BC break (because this is a protected method), but I think that it's better to remove it has it has never be intended to be used, it's just a miss. An alternative is to deprecate it and remove it in v4.

    Commits
    -------

    fea18aa [Serializer] Remove AbstractObjectNormalizer::isAttributeToNormalize