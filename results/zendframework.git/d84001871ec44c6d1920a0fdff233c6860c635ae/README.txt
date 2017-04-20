commit d84001871ec44c6d1920a0fdff233c6860c635ae
Author: Matthew Weier O'Phinney <matthew@zend.com>
Date:   Fri Oct 22 12:55:49 2010 -0400

    AbstractBootstrap tests

    - Removed obsolete/irrelevant tests
    - Added new tests for broker functionality, including ability to inject broker
      via options
    - Marked several tests as disabled; most deal with PluginLoader, and either need
      to be refactored or removed
      - Noting that it would be useful to be able to do injection of class loader
        and class loader options via configuration