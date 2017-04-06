commit 3625a6efc1a5b5d7a5111e7fa59d5a3b619be877
Merge: fc557db d81da79
Author: Fabien Potencier <fabien.potencier@gmail.com>
Date:   Fri Nov 4 08:00:34 2016 -0700

    minor #20405 [SecurityBundle] Display firewall in debug bar even if not authenticated (chalasr)

    This PR was squashed before being merged into the 3.2-dev branch (closes #20405).

    Discussion
    ----------

    [SecurityBundle] Display firewall in debug bar even if not authenticated

    | Q             | A
    | ------------- | ---
    | Branch?       | master
    | Tests pass?   | yes
    | License       | MIT

    Before:
    ![before](http://image.prntscr.com/image/897d1f2b43d64c5a8e15a95bf927a01c.png)

    After:
    ![after](http://image.prntscr.com/image/4491a3aea6fe44dd8ca24a2b25a37596.png)

    I will take any input to improve the result, I feel it not optimal.

    Commits
    -------

    d81da79 [SecurityBundle] Display firewall in debug bar even if not authenticated