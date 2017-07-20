commit 59af4d325892827586e72015a0dd1cc797344924
Author: phpnut <phpnut@cakephp.org>
Date:   Mon Sep 19 17:20:24 2005 +0000

    Merging changes from:

    [905]
    Refactoring renderMethod() moving to object class to make it available to whole system

    [908]
    Bug fixes to ticket #219.
    Added bug fixes I found in code.
    Fixed Router::parse(); which only checked lower case strings
    Refactored renderAction method. Moved to object class to allow all classes to access the method now.
    This will allow a User object to make a request to a Permsissions object checking.
    For example: You have a User object that needs to check its permission to access
    User object would make are request to Permission object.
    $accces = $this->renderAction('/permission/allow/userid/');

    You would then be able to use the results of $access in your controller.
    To check this in the view
    $accces = $html->renderAction('/permission/allow/userid/');

    Simple as that.

    [906]
    Added sortable() to AjaxHelper as a method of creating sortables,
    and refactored a fair amount of the code.

    [907]
    Coding standard corrections.

    removed blank line at end of paths.php file

    git-svn-id: https://svn.cakephp.org/repo/trunk/cake@910 3807eeeb-6ff5-0310-8944-8be069107fe0