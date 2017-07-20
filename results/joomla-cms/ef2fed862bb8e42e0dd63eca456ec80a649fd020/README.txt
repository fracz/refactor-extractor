commit ef2fed862bb8e42e0dd63eca456ec80a649fd020
Author: Johan Janssens <johan@nooku.org>
Date:   Wed Feb 7 00:36:57 2007 +0000

    Major refactoring of Itemid handling
    Implemented JRouter in application package and improved routing algorithms
    Reworked JMenu class to allow lookups based on itemid routes
    Changed component request.php to route.php to better refect routing system
    Split application render function into route and dispatch
    Added onAfterDispatch and onAfterRoute system events
    Removed onAfterRender system event
    Added alias support to com_categories and com_sections
    Changes require a reinstall

    git-svn-id: http://joomlacode.org/svn/joomla/development/trunk@6521 6f6e1ebd-4c2b-0410-823f-f34bde69bce9