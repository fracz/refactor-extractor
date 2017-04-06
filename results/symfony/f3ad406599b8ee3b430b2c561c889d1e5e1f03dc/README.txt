commit f3ad406599b8ee3b430b2c561c889d1e5e1f03dc
Merge: ec82e32 97a8f7e
Author: Fabien Potencier <fabien.potencier@gmail.com>
Date:   Sun Nov 2 02:05:26 2014 +0100

    minor #12377 Translation debug improve error reporting (mrthehud, fabpot)

    This PR was merged into the 2.3 branch.

    Discussion
    ----------

    Translation debug improve error reporting

    | Q             | A
    | ------------- | ---
    | Bug fix?      | no
    | New feature?  | no
    | BC breaks?    | no
    | Deprecations? | no
    | Tests pass?   | yes
    | Fixed tickets | #12252
    | License       | MIT
    | Doc PR        |

    Indicate which file was being parsed if an exception is thrown while running translation:debug

    When running the translation:debug command, if a template contains invalid twig markup,
    an exception is thrown. This patch rethrows a new exception that includes the filename
    being parsed in the message to aid debugging.

    Commits
    -------

    97a8f7e [TwigBundle] added a test
    b1bffc0 Indicate which file was being parsed if an exception is thrown while running translation:debug