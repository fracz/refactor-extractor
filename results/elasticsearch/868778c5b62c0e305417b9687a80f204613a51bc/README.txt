commit 868778c5b62c0e305417b9687a80f204613a51bc
Author: Michael McCandless <mail@mikemccandless.com>
Date:   Thu Oct 2 06:31:45 2014 -0400

    Core: improve build_release.py

    Don't insist on log file removal until after usage is printed.

    Some simple Python code improvements (x.find(y) != -1 --> y in x)

    Make sure the git area is "clean" (has no unpushed changes, has pulled
    all changes, has no untracked files)

    Add label color detail when creating next github version label.

    Closes #7913