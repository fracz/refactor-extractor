commit 3502ca22a6db88388479393cd408464a52e7004e
Merge: 7455650 5798029
Author: Fabien Potencier <fabien.potencier@gmail.com>
Date:   Thu Oct 31 17:40:24 2013 +0100

    minor #9244 [Console] make parent constructor test more reliable (Tobion)

    This PR was merged into the master branch.

    Discussion
    ----------

    [Console] make parent constructor test more reliable

    | Q             | A
    | ------------- | ---
    | Bug fix?      | yes
    | New feature?  | no
    | BC breaks?    | no
    | Deprecations? | no
    | Fixes   | #9186 , f2b60e9
    | Tests pass?   | yes
    | license?      | MIT

    It also fixes the test since f2b60e9 and improves phpdoc

    The second commit improves regex performance to validate name (using possesive quantifier).

    I did some basic performance tests http://3v4l.org/PuvuL
    The new regex only takes 1/3 of the time compared to the old one!

    Commits
    -------

    5798029 [Console] improve regex performance to validate name
    22b09ce [Console] make parent constructor test more reliable