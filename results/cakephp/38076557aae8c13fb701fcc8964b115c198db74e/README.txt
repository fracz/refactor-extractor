commit 38076557aae8c13fb701fcc8964b115c198db74e
Author: phpnut <phpnut@cakephp.org>
Date:   Mon Oct 24 03:44:54 2005 +0000

    [1183]
    Author: phpnut
    Date: 10:42:06 PM, Sunday, October 23, 2005
    Message:
    Fixed errors created with change I have been making.
    Controller::generateFields(); was not setting the tagName properly.

    [1182]
    Author: phpnut
    Date: 10:13:33 PM, Sunday, October 23, 2005
    Message:
    Removing the need to set hasMany and hasAndBelongsToMany to plural.
    All associations should be created as CamelCase associations now.

    [1181]
    Author: phpnut
    Date: 9:38:50 PM, Sunday, October 23, 2005
    Message:
    Forgot dispatcher.php in last commit

    [1180]
    Author: phpnut
    Date: 9:37:06 PM, Sunday, October 23, 2005
    Message:
    refactoring and removing unneeded calls to Inflector


    git-svn-id: https://svn.cakephp.org/repo/trunk/cake@1184 3807eeeb-6ff5-0310-8944-8be069107fe0