commit 38c059fc0374d6cba566cb9c3d54d1da2b990f9a
Merge: 91cab2e d128735
Author: Fabien Potencier <fabien.potencier@gmail.com>
Date:   Sat Oct 3 08:28:13 2015 +0200

    bug #16094 Fix the crawler refactoring (stof)

    This PR was merged into the 3.0-dev branch.

    Discussion
    ----------

    Fix the crawler refactoring

    | Q             | A
    | ------------- | ---
    | Bug fix?      | yes
    | New feature?  | no
    | BC breaks?    | no
    | Deprecations? | no
    | Tests pass?   | yes
    | Fixed tickets | n/a
    | License       | MIT
    | Doc PR        | n/a

    This fixes a few mistakes I spotted in #16075 for the DomCrawler component.

    Regression tests are added separately in https://github.com/symfony/symfony/pull/16093 to be included in older branches too.

    Commits
    -------

    d128735 Fix the crawler refactoring