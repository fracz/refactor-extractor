commit ea8359a870b98dba1d6b999baebe8f0420dbdc21
Merge: b0b109b 5499a29
Author: Fabien Potencier <fabien.potencier@gmail.com>
Date:   Tue Sep 10 19:41:59 2013 +0200

    merged branch bschussek/constraint-annotation-improvement (PR #8979)

    This PR was merged into the master branch.

    Discussion
    ----------

    [Validator] The default option name can now be omitted when defining constraints as annotations

    | Q             | A
    | ------------- | ---
    | Bug fix?      | no
    | New feature?  | yes
    | BC breaks?    | no
    | Deprecations? | no
    | Tests pass?   | yes
    | Fixed tickets | -
    | License       | MIT
    | Doc PR        | -

    When a constraint has a default option, but other options should be set as well, the name of the default option had to be set explicitly when defining the constraint as annotation:

    ```php
    /** @GreaterThan(value=2, message="Should be greater than 2") */
    private $value;
    ```

    After this PR, it is possible to omit the name of the default option:

    ```php
    /** @GreaterThan(2, message="Should be greater than 2") */
    private $value;
    ```

    Commits
    -------

    5499a29 [Validator] The default option name can now be omitted when defining constraints as annotations