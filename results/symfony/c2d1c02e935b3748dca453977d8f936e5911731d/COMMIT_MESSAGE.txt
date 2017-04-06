commit c2d1c02e935b3748dca453977d8f936e5911731d
Merge: aa770e1 d642eae
Author: Fabien Potencier <fabien.potencier@gmail.com>
Date:   Fri Feb 26 06:32:11 2016 +0100

    feature #17531 [PropertyInfo] Use last version of reflection docblock (joelwurtz)

    This PR was merged into the 3.1-dev branch.

    Discussion
    ----------

    [PropertyInfo] Use last version of reflection docblock

    | Q             | A
    | ------------- | ---
    | Bug fix?      | no
    | New feature?  | no
    | BC breaks?    | no
    | Deprecations? | no
    | Tests pass?   | yes
    | Fixed tickets |
    | License       | MIT
    | Doc PR        |

    This PR improve the PhpDocExtractor to use https://github.com/phpDocumentor/ReflectionDocBlock 3.0 dev branch instead of https://github.com/phpDocumentor/ReflectionDocBlock 1.0.7 which is not maintained since 2014

    I don't know if it's a BC break since it's only a suggested dependency.

    This is somehow mandatory (we can maybe use a more stable, but also not maintained version of this repository) for #17516 as it does not have a dependency on php-parser 0.9.4 which is very old and not really suitable for the new component.

    Commits
    -------

    d642eae Use last version of reflection dockblock, avoid extra dependancy if library needed