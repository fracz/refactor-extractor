commit 7fec2c9a3e76bdaf6f2fe532d81ab01bd37c048f
Merge: aee0336 d902e9d
Author: Fabien Potencier <fabien.potencier@gmail.com>
Date:   Thu Dec 6 13:46:50 2012 +0100

    merged branch nomack84/route_debug_cmd (PR #6205)

    This PR was merged into the master branch.

    Commits
    -------

    d902e9d [FrameworkBundle] Added hostnamePattern to the router:debug command

    Discussion
    ----------

    [FrameworkBundle] Added hostnamePattern to the router:debug command

    Bug fix: yes
    Feature addition: no
    Backwards compatibility break: no
    Symfony2 tests pass: yes
    Fixes the following tickets: -
    Todo: -
    License of the code: MIT

    When you use the router:debug command with a specific route, the hostname is not show. I fix this with this PR.  Also I make a little improvement to the requirements section.