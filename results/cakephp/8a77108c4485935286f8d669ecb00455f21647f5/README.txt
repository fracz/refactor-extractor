commit 8a77108c4485935286f8d669ecb00455f21647f5
Author: phpnut <phpnut@cakephp.org>
Date:   Mon Jan 16 21:34:46 2006 +0000

    Merging fixes to trunk

    Revision: [1809]
    Fixed self join code, may refactor after looking at it more. Currently it is working as expected.
    Fixed errors in scaffold view caused by the changes I made.
    Removed adding Child_ prefix to self joined associations.

    Revision: [1808]
    Adding changes I started on the self join code.

    Revision: [1807]
    Adding patch from Ticket #283.
    Changed doc comment in SessionComponent class.
    Added fix for Ticket #285


    git-svn-id: https://svn.cakephp.org/repo/trunk/cake@1810 3807eeeb-6ff5-0310-8944-8be069107fe0