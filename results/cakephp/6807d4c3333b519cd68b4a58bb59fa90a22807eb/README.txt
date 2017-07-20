commit 6807d4c3333b519cd68b4a58bb59fa90a22807eb
Author: phpnut <phpnut@cakephp.org>
Date:   Tue Jun 10 22:38:05 2008 +0000

    Starting initial refactoring of current code base.
    Made changes to test suite to allow running test without headers already sent errors.
    Moved Component::initialize(), Controller::beforeFilter(); and Component::startup(); from Dipatcher::start() to Controller::constructClasses();
    Removed Dispatcher::start();
    Fixing model instances not being created
    Adding additional test to CookieComponent to increase coverage to 95%
    Optimizing Set::diff();
    Fixing SessionComponent test and RequestHandlerComponent test
    Fixing CakeSession tests, removed deprecated code from CakeSession

    git-svn-id: https://svn.cakephp.org/repo/branches/1.2.x.x@7162 3807eeeb-6ff5-0310-8944-8be069107fe0