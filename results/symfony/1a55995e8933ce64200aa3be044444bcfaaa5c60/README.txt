commit 1a55995e8933ce64200aa3be044444bcfaaa5c60
Merge: 05815ad b601454
Author: Fabien Potencier <fabien.potencier@gmail.com>
Date:   Mon Sep 22 14:29:55 2014 +0200

    feature #11311 [FrameworkBundle] Additional helper commands to control PHP's built-in web server (xabbuh)

    This PR was merged into the 2.6-dev branch.

    Discussion
    ----------

    [FrameworkBundle] Additional helper commands to control PHP's built-in web server

    | Q             | A
    | ------------- | ---
    | Bug fix?      | no
    | New feature?  | yes
    | BC breaks?    | no
    | Deprecations? | no
    | Tests pass?   | yes
    | Fixed tickets | #10827
    | License       | MIT
    | Doc PR        | symfony/symfony-docs#4005

    Basically, both the ``server:status`` and ``server:stop`` wouldn't be really reliable if you had stopped the web server by, for example, killing the process. But honestly I don't know how to platform-independently determine if a process is still running given its PID. Maybe such a way could be a good improvement for the Process component.

    Commits
    -------

    b601454 new helper commands for PHP's built-in server