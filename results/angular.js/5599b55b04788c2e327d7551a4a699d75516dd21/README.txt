commit 5599b55b04788c2e327d7551a4a699d75516dd21
Author: Igor Minar <igor@angularjs.org>
Date:   Wed Jun 5 15:30:31 2013 -0700

    refactor($route): pull $route and friends into angular-route.js

    $route, $routeParams and ngView have been pulled from core angular.js
    to angular-route.js/ngRoute module.

    This is was done to in order keep the core focused on most commonly
    used functionality and allow community routers to be freely used
    instead of $route service.

    There is no need to panic, angular-route will keep on being supported
    by the angular team.

    Note: I'm intentionally not fixing tutorial links. Tutorial will need
    bigger changes and those should be done when we update tutorial to
    1.2.

    BREAKING CHANGE: applications that use $route will now need to load
    angular-route.js file and define dependency on ngRoute module.

    Before:

    ```
    ...
    <script src="angular.js"></script>
    ...
    var myApp = angular.module('myApp', ['someOtherModule']);
    ...
    ```

    After:

    ```
    ...
    <script src="angular.js"></script>
    <script src="angular-route.js"></script>
    ...
    var myApp = angular.module('myApp', ['ngRoute', 'someOtherModule']);
    ...
    ```

    Closes #2804