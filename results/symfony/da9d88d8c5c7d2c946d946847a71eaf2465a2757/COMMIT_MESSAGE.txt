commit da9d88d8c5c7d2c946d946847a71eaf2465a2757
Merge: 8936c33 ea80c9b
Author: Fabien Potencier <fabien.potencier@gmail.com>
Date:   Fri Jan 16 16:11:56 2015 +0100

    feature #13418 [DX] Attempt to improve logging messages with  parameters (iltar)

    This PR was squashed before being merged into the 2.7 branch (closes #13418).

    Discussion
    ----------

    [DX] Attempt to improve logging messages with  parameters

    | Q             | A
    | ------------- | ---
    | Bug fix?      | no
    | New feature?  | no
    | BC breaks?    | no
    | Deprecations? | no
    | Tests pass?   | yes
    | Fixed tickets | https://github.com/symfony/symfony/pull/12594#issuecomment-68589400
    | License       | MIT
    | Doc PR        | n/a

    This PR is a follow-up of #12594 `[DX] [HttpKernel] Use "context" argument when logging route in RouterListener`.

    I have attempted to improve the log messages, as well as updating the usage context. I wasn't sure if the log messages should end with a `.` or not, if so I can update all messages to confirm a standard.

    Commits
    -------

    ea80c9b [DX] Attempt to improve logging messages with  parameters