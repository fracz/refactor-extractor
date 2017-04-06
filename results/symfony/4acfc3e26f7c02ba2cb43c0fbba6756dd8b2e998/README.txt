commit 4acfc3e26f7c02ba2cb43c0fbba6756dd8b2e998
Merge: 4a177d7 834218e
Author: Fabien Potencier <fabien.potencier@gmail.com>
Date:   Thu Nov 5 15:03:06 2015 +0100

    feature #11431 [Console] End of options (--) signal support (Seldaek)

    This PR was squashed before being merged into the 3.0-dev branch (closes #11431).

    Discussion
    ----------

    [Console] End of options (--) signal support

    | Q             | A
    | ------------- | ---
    | Bug fix?      | yes
    | New feature?  | yes
    | BC breaks?    | yes-ish
    | Deprecations? | no
    | Tests pass?   | yes
    | Fixed tickets |
    | License       | MIT
    | Doc PR        |

    The first commit is only about -- support + fixes a few other little issues I discovered while adding tests to cover my feature.
    The second commit makes use of the first in the Application class, so that if you do `console foo -- --help` for example, it should not trigger the help anymore, but simply have --help available as a dummy arg.

    To keep BC, I did this by adding a param to hasParameterOption/getParameterOption instead of doing it by default. This creates another BC issue though in that it changes the InputInterface. I don't think that's a major concern but it's worth mentioning.

    The other only minor BC change is if you do getParameterOption() of a flag option i.e. `--foo` without a value. Before it was returning null in ArgvInput and true in ArrayInput. I normalized this to true for both since I think that makes more sense, but strictly speaking while it improves interface interchangeability, it's a BC break.

    Commits
    -------

    834218e [Console] End of options (--) signal support