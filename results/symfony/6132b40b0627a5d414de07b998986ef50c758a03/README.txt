commit 6132b40b0627a5d414de07b998986ef50c758a03
Merge: 32947b2 4c9e606
Author: Fabien Potencier <fabien.potencier@gmail.com>
Date:   Wed Aug 28 20:20:21 2013 +0200

    merged branch endroid/master (PR #8488)

    This PR was squashed before being merged into the master branch (closes #8488).

    Discussion
    ----------

    Add container aware trait

    | Q             | A
    | ------------- | ---
    | Bug fix?      | no
    | New feature?  | yes
    | BC breaks?    | no
    | Deprecations? | no
    | Tests pass?   | yes
    | Fixed tickets |
    | License       | MIT
    | Doc PR        | will create one if merged

    This trait can be used inside a class to make it container aware. As it is not always possible to extend the abstract ContainerAware class, the code for creating a container aware class is usually copied over and over (most of the time unchanged), resulting in a lot of redundant code in your project. As my projects are PHP 5.4 based nowadays, I would like to be able to use a trait in these scenarios to improve my workflow.

    Commits
    -------

    4c9e606 Add container aware trait