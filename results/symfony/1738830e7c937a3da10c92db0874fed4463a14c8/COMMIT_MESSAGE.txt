commit 1738830e7c937a3da10c92db0874fed4463a14c8
Merge: 14d6587 07a1e70
Author: Fabien Potencier <fabien.potencier@gmail.com>
Date:   Sun Nov 2 01:18:24 2014 +0100

    feature #12355 [VarDumper] Use symfony.com's colorscheme (nicolas-grekas, WouterJ)

    This PR was merged into the 2.6-dev branch.

    Discussion
    ----------

    [VarDumper] Use symfony.com's colorscheme

    As promised, this PR changed the current colorscheme to something used in Symfony. This means developers have a consistent experience and the colorscheme is easier to read.

    While doings this, I also improved the `HtmlDumper#style()` method to make it easier to understand and maintain. I also changed a bit of the output it created, mostly adding visibility prefixes to properties.

    **Before**
    ![sf-dump-before](https://cloud.githubusercontent.com/assets/749025/4828241/b070d32e-5f7e-11e4-9d51-865ffd47753e.png)

    **After**
    ![sf-dump-after](https://cloud.githubusercontent.com/assets/749025/4828244/b4ec3e52-5f7e-11e4-9031-71f41bc30c25.png)

    | Q             | A
    | ------------- | ---
    | Bug fix?      | yes
    | New feature?  | yes
    | BC breaks?    | -
    | Deprecations? | -
    | Tests pass?   | -
    | Fixed tickets | #12348
    | License       | MIT
    | Doc PR        | the one about the VarDumper

    Commits
    -------

    07a1e70 Changed meta color
    a316420 [VarDumper] use symfony.com colorscheme
    7ffba44 [VarDumper] UML prefixes for properties