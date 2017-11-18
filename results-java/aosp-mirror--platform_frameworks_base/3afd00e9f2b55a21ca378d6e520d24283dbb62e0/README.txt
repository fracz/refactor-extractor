commit 3afd00e9f2b55a21ca378d6e520d24283dbb62e0
Author: Selim Cinek <cinek@google.com>
Date:   Mon Aug 11 22:32:57 2014 +0200

    Fixed a bug in the notification stack algorithm

    Bad holes could occur when a notification was at the same time in
    the top and the bottom stack. This also improves the landscape /
    smallscreen interaction with the shade.

    Bug: 16715133
    Change-Id: Icbb4d080e658f4ddbd39b7d08652ca5311a47978