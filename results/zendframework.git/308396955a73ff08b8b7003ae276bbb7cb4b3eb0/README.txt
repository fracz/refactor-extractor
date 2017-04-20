commit 308396955a73ff08b8b7003ae276bbb7cb4b3eb0
Author: Matthew Weier O'Phinney <matthew@zend.com>
Date:   Mon Apr 23 10:56:00 2012 -0500

    Do not auto-inject SharedEventManager

    ...at least not until we have Application/Bootstrap refactored.
    - Instead, define the SharedEventManager constructor to be the
      StaticEventManager singleton accessor