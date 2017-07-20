commit fc0854e3979b4d624e42a015f3802410e2b333f3
Author: phpnut <phpnut@cakephp.org>
Date:   Tue Mar 14 02:26:08 2006 +0000

    Merging fixes and enhancements into trunk.

    Revision: [2287]
    Merging changes from model_php4.php

    Revision: [2286]
    Added serialized object data to the cached file.
    Instances of the view helpers are available in the views now.
    You also have access to the Controller::<component>, example $this->controller->Session;

    Revision: [2285]
    Adding Controller::postConditions() to convert a POST'ed data array to a Model conditions array

    Revision: [2284]
    Adding Model::invalidate() and refactoring Model::invalidFields().  Adding a fix for RequestHandler::accepts()

    git-svn-id: https://svn.cakephp.org/repo/trunk/cake@2288 3807eeeb-6ff5-0310-8944-8be069107fe0