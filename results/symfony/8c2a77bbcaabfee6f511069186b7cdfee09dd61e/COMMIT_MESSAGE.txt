commit 8c2a77bbcaabfee6f511069186b7cdfee09dd61e
Merge: b550d7e 73fbd08
Author: Nicolas Grekas <nicolas.grekas@gmail.com>
Date:   Fri Nov 25 13:07:11 2016 +0100

    bug #20474 [Routing] Fail properly when a route parameter name cannot be used as a PCRE subpattern name (fancyweb)

    This PR was squashed before being merged into the 2.7 branch (closes #20474).

    Discussion
    ----------

    [Routing] Fail properly when a route parameter name cannot be used as a PCRE subpattern name

    | Q             | A
    | ------------- | ---
    | Branch?       | 2.7
    | Bug fix?      | no
    | New feature?  | no
    | BC breaks?    | no
    | Deprecations? | no
    | Tests pass?   | yes
    | Fixed tickets | -
    | License       | MIT
    | Doc PR        | https://github.com/symfony/symfony-docs/pull/7139

    This is a follow up PR to https://github.com/symfony/symfony/pull/20327.

    I also decided to improve the current `is_numeric()` check that is done by a truer one. The current limitation is that a PCRE subpattern name must not start with a digit which is different.

    Commits
    -------

    73fbd08 [Routing] Fail properly when a route parameter name cannot be used as a PCRE subpattern name