commit 147bca3d2235a316c58d9a3644f7a9e46794be05
Author: Mikhail Glukhikh <Mikhail.Glukhikh@jetbrains.com>
Date:   Fri May 8 10:29:51 2015 +0300

    Enum deprecated syntax detection implemented and integrated into DeclarationsChecker.

    A lot of tests was changed to refactor deprecated syntax. Six new tests were added to check deprecated syntax detection.
    Diagnostic for "enum entry uses deprecated super constructor": constructor is highlighted
    Diagnostic for "enum entry uses deprecated or no delimiter".
    One warning removed.