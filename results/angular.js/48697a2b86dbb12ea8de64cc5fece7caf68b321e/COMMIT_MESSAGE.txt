commit 48697a2b86dbb12ea8de64cc5fece7caf68b321e
Author: Misko Hevery <misko@hevery.com>
Date:   Mon Oct 17 16:56:56 2011 -0700

    refactor(injector): turn scope into a service

    - turn scope into a $rootScope service.
    - injector is now a starting point for creating angular application.
    - added inject() method which wraps jasmine its/beforeEach/afterEach,
      and which allows configuration and injection of services.
    - refactor tests to use inject() where possible

    BREAK:
    - removed angular.scope() method