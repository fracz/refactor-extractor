commit 99c9ed48fb103df3043cc88dd38d014663387fc0
Author: Adam Powell <adamp@google.com>
Date:   Tue Jul 16 11:35:17 2013 -0700

    Fix collapsible action views.

    Fix a regression caused by a previous overzealous refactoring. (Oops.)
    Watch those conditionals, everyone!

    Bug 9866559

    Change-Id: Ia88a4ee38edef378e70bdc7151c825375a3d482d