commit 91525f00aba2f0ce0e5a62c914081f6cdeaf497b
Author: phpnut <phpnut@cakephp.org>
Date:   Sun Feb 19 22:14:21 2006 +0000

    Merging fixes and enhancements into trunk.

    Revision: [2056]
    Corrected typo in a variable name

    Revision: [2055]
    Added regex to remove ORDER BY if it is passed in the string

    Revision: [2054]
    A little refactoring to changes made in [2053]

    Revision: [2053]
    Adding fix for Ticket #413

    Revision: [2052]
    Reverting changes made in [2050]

    Revision: [2051]
    Fixing Ticket #410

    Revision: [2050]
    Fixing Ticket #409, and adding additional column functions

    Revision: [2049]
    Removing old DBO class. Fixes Ticket #408
    Updated all Dbo* database classes to extent DboSource.
    Corrected formatting of code in CakeSession

    Revision: [2048]
    Adding fix for Ticket #411.
    Updating variables and method to use coding standards.
    Updated CakeSession doc blocks

    git-svn-id: https://svn.cakephp.org/repo/trunk/cake@2057 3807eeeb-6ff5-0310-8944-8be069107fe0