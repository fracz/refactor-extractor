commit fb79c134ad4df0b09115884f5534ef0ea5c85bb9
Author: Matthew Weier O'Phinney <matthew@zend.com>
Date:   Wed Apr 17 14:39:09 2013 -0500

    Make independent factories for HTTP and Console ViewManagers

    - Created `HttpViewManagerFactory` and `ConsoleViewManagerFactory`; each
      will return the requested view manager.
    - `ViewManagerFactory` now consumes the above two factories (instead of
      using direct instantiation).

    This will allow console endpoints to utilize the HTTP view manager if
    desired by doing the following:

    ```php
    'service_manager' => array(
        'factories' => array(
            'ViewManager' => 'Zend\Mvc\Service\HttpViewManager',
        ),
    ),
    ```

    I plan further refactoring to move the event listener registration into
    individual factories as well.