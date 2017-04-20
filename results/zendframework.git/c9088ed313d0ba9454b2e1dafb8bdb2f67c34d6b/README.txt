commit c9088ed313d0ba9454b2e1dafb8bdb2f67c34d6b
Author: Matthew Weier O'Phinney <matthew@zend.com>
Date:   Wed May 19 12:03:14 2010 -0500

    [session] Updated DBTable save handler

    - Fixed issue in PDO\SQLite adapter to make it compile
    - Updated DbTable session save handler to use refactored Zend\DB component