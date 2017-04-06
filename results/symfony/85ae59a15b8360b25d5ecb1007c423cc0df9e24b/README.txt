commit 85ae59a15b8360b25d5ecb1007c423cc0df9e24b
Merge: be691d5 93ab017
Author: Fabien Potencier <fabien.potencier@gmail.com>
Date:   Thu Feb 16 03:30:17 2017 -0800

    minor #21536 [TwigBridge] Add docblocks for Twig url and path function to improve ide completion (Haehnchen)

    This PR was merged into the 2.7 branch.

    Discussion
    ----------

    [TwigBridge] Add docblocks for Twig url and path function to improve ide completion

    | Q             | A
    | ------------- | ---
    | Branch?       | 2.7
    | Bug fix?      | no
    | New feature?  | no
    | BC breaks?    | no
    | Deprecations? | no
    | Tests pass?   | yes
    | License       | MIT

    `RoutingExtension` missed docblock for Twig extension so `path` and `url` are not fully recognized by PhpStorm.

    https://github.com/Haehnchen/idea-php-symfony2-plugin/issues/864 will add more smarter completion
    `{{ p<caret> }} -> {{ path('<caret>') }}`, but string parameter is not detected. This will add the minimal docs

    Commits
    -------

    93ab0179f0 add docblocks for Twig url and path function to improve ide completion