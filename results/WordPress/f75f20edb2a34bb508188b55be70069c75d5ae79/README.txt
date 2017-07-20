commit f75f20edb2a34bb508188b55be70069c75d5ae79
Author: Andrea Fercia <a.fercia@gmail.com>
Date:   Thu Feb 4 19:19:27 2016 +0000

    Accessibility: simplify the Plugins and Themes tables on the Updates screen.

    Although it may seem counterintuitive at first, in very limited cases it's
    better to remove improper semantics (this is not a tabular data table) in
    order to reduce noise for screen reader users and simplify all the things.
    Also improves headings to better separate sections.

    Fixes #34780.
    Built from https://develop.svn.wordpress.org/trunk@36477


    git-svn-id: http://core.svn.wordpress.org/trunk@36444 1a063a9b-81f0-0310-95a4-ce76da25c4cd