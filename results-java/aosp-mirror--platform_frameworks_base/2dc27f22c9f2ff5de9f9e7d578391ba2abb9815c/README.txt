commit 2dc27f22c9f2ff5de9f9e7d578391ba2abb9815c
Author: Adam Powell <adamp@google.com>
Date:   Tue Jul 16 11:35:17 2013 -0700

    Fix collapsible action views.

    Fix a regression caused by a previous overzealous refactoring. (Oops.)
    Watch those conditionals, everyone!

    Bug 9866559

    Change-Id: Ia88a4ee38edef378e70bdc7151c825375a3d482d