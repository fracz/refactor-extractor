commit 3fb3d7c4e756bd32d5abde0abca9ab52d559bc84
Author: Adam Powell <adamp@google.com>
Date:   Fri Apr 22 17:08:55 2011 -0700

    Revert "Touch exploration feature, event bubling, refactor"

    This reverts commit ac84d3ba81f08036308b17e1ab919e43987a3df5.

    There seems to be a problem with this API change. Reverting for now to
    fix the build.

    Change-Id: Ifa7426b080651b59afbcec2d3ede09a3ec49644c