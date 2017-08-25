commit bddce23e1b2c4ca4bd66085a8416b456bccd2037
Author: David Grudl <david@grudl.com>
Date:   Fri Jul 4 08:23:06 2008 +0000

    - improved exception messages in several classes
    - removed SimpleRouter dependency from MultiRouter (moved to Application)
    - new InvalidPresenterException, BadRequestException
    - PresenterLoader caches its operation
    - IPermissionAssert -> IPermissionAssertion