commit 3d67aba24a7f777739da2db1b296593cd61e2339
Merge: c6e1a49 c7a44be
Author: Fabien Potencier <fabien.potencier@gmail.com>
Date:   Sun Mar 5 12:27:24 2017 -0800

    feature #21421 Use proper error message when session write fails #20807 (digilist)

    This PR was submitted for the 2.8 branch but it was merged into the 3.3-dev branch instead (closes #21421).

    Discussion
    ----------

    Use proper error message when session write fails #20807

    This improves the error message that is thrown if a session write fails (see #20807)

    As there was no way to get the actual handler, I introduced a method on the SessionHandlerProxy. I hope that's okay, otherwise please give me a hint on what to do.

    | Q             | A
    | ------------- | ---
    | Branch?       | 2.8
    | Bug fix?      | no
    | New feature?  | no
    | BC breaks?    | no
    | Deprecations? | no
    | Tests pass?   | yes
    | Fixed tickets | #20807
    | License       | MIT

    Commits
    -------

    c7a44be4b1 Use proper error message when session write fails #20807