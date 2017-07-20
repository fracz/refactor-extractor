commit b06537e351aea47373d901d896108ad62b37a70f
Author: lsmith77 <smith@pooteeweet.org>
Date:   Tue Apr 17 10:08:13 2012 +0200

    refactored code to use get() when outputting a single route

    this is useful for a CMS, where in most cases there will be too many routes to make it feasible to load all of them. here a router implementation will be used that will return an empty collection for ->all(). with this refactoring the given routes will not be listed via router:debug, but would still be shown when using router:debug [name]