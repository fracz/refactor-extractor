commit fc9fa42978c79a0167baa3aaac553b278a1fb93d
Author: Matthew Weier O'Phinney <matthew@zend.com>
Date:   Tue Mar 30 09:04:25 2010 -0400

    [NS] Zend_Cache refactoring
    - Created Backend, Frontend interfaces
    - Renamed "Extended" interface to ExtendedBackend
    - Moved Core to Frontend\Core
    - Fixed Zend\Locale to typehint on Zend\Cache\Frontend and default to
      Frontend\Core