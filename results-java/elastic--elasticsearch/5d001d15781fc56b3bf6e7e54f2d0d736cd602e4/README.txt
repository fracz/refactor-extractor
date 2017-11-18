commit 5d001d15781fc56b3bf6e7e54f2d0d736cd602e4
Author: Robert Muir <rmuir@apache.org>
Date:   Wed Oct 14 14:46:45 2015 -0400

    Decentralize plugin security

    * Add ability for plugins to declare additional permissions with a custom plugin-security.policy file and corresponding AccessController logic. See the plugin author's guide for more information.
    * Add warning messages to users for extra plugin permissions in bin/plugin.
    * When bin/plugin is run interactively (stdin is a controlling terminal and -b/--batch not supplied), require user confirmation.
    * Improve unit test and IDE support for plugins with additional permissions by exposing plugin's metadata as a maven test resource.

    Closes #14108

    Squashed commit of the following:

    commit cf8ace65a7397aaccd356bf55f95d6fbb8bb571c
    Author: Robert Muir <rmuir@apache.org>
    Date:   Wed Oct 14 13:36:05 2015 -0400

        fix new unit test from master merge

    commit 9be3c5aa38f2d9ae50f3d54924a30ad9cddeeb65
    Merge: 2f168b8 7368231
    Author: Robert Muir <rmuir@apache.org>
    Date:   Wed Oct 14 12:58:31 2015 -0400

        Merge branch 'master' into off_my_back

    commit 2f168b8038e32672f01ad0279fb5db77ba902ae8
    Author: Robert Muir <rmuir@apache.org>
    Date:   Wed Oct 14 12:56:04 2015 -0400

        improve plugin author documentation

    commit 6e6c2bfda68a418d92733ac22a58eec35508b2d0
    Author: Robert Muir <rmuir@apache.org>
    Date:   Wed Oct 14 12:52:14 2015 -0400

        move security confirmation after 'plugin already installed' check, to prevent user from answering unnecessary questions.

    commit 08233a2972554afef2a6a7521990283102e20d92
    Author: Robert Muir <rmuir@apache.org>
    Date:   Wed Oct 14 05:36:42 2015 -0400

        Add documentation and pluginmanager support

    commit 05dad86c51488ba43ccbd749f0164f3fbd3aee62
    Author: Robert Muir <rmuir@apache.org>
    Date:   Wed Oct 14 02:22:24 2015 -0400

        Decentralize plugin permissions (modulo docs and pluginmanager work)