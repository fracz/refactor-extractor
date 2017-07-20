commit 9f9c84de5250f8ff986e9934960d01025630de24
Author: phpnut <phpnut@cakephp.org>
Date:   Tue Mar 14 11:54:03 2006 +0000

    Merging fixes and enhancements into trunk Added fix for Ticket #492.

    Revision: [2299]
    Added fix for Ticket #494.
    Added fix for Ticket #489.

    Revision: [2298]
    Fixed regex that would match more than one <cake:nocache></cake:nocache> group and cause a  wrong replace of the tags

    Revision: [2297]
    Corrected error in the convertSlash() function

    Revision: [2296]
    Another bug found and corrected after last commit.
    This is working as expected now, and will be refactored.

    Revision: [2295]
    Fixed undefined index in CacheHelper::__parseFile();
    Fixed caching when method is cached without params being set in the key

    Revision: [2294]
    Corrected caching for index method when accessing www.example.com/controller this would not use default index method.
    Removed auto setting of the data variable in Controller::render();
    Added additional check for cached index view in bootstrap.php

    Revision: [2293]
    Refactoring caching code.
    Fixed problem with files being cached for all methods.
    Added code to cache files to create helpers.
    Moved duplicate code to basics.php as a function

    git-svn-id: https://svn.cakephp.org/repo/trunk/cake@2300 3807eeeb-6ff5-0310-8944-8be069107fe0