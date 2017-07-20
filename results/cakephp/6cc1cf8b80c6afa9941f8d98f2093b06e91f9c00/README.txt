commit 6cc1cf8b80c6afa9941f8d98f2093b06e91f9c00
Author: phpnut <phpnut@cakephp.org>
Date:   Tue Feb 28 07:35:01 2006 +0000

    Merging fixes and enhancements into trunk.

    Revision: [2151]
    Merging changes made to model_php5.php into model_php4.php.

    Revision: [2150]
    Cleaning up code, removing in line comments

    Revision: [2149]
    Fixed wrong params passed to SessionComponent::setFlash() in Scaffold class.
    Changed View::_render() to only suppress errors in the view when DEBUG is set to 0.

    Revision: [2148]
    Adding suggestion from Ticket #446.

    Revision: [2147]
    Added fix for Ticket #443

    Revision: [2146]
    Added fix for Ticket #444.
    Added table.field in the CakeSession class database methods

    Revision: [2145]
    Renamed _new to _blank in default.thtml.
    Starting to refactor changes that broke self joined associations.

    Revision: [2144]
    Adding fix for Ticket #202

    Revision: [2143]
    Adding support for nested array-based conditions

    Revision: [2141]
    Updating View for Session flash changes

    Revision: [2140]
    Adding Session flash enhancements for a ticket that I took but can't find because someone else closed it

    git-svn-id: https://svn.cakephp.org/repo/trunk/cake@2152 3807eeeb-6ff5-0310-8944-8be069107fe0