commit dc98d909ab14a99aa1874770cde352c0d32d0250
Author: Brian Clozel <bclozel@pivotal.io>
Date:   Wed Feb 22 14:29:59 2017 +0100

    Support reactive web servers with LocalServerPort

    This commit refactors the `EmbeddedWebServerInitializedEvent` hierarchy
    to have one specialized event for Servlet based apps and another one for
    reactive apps.

    Each event implementation has:

    * a specific `ApplicationContext` implementation for the app
    * a custom `getServerId` implementation that differentiates the
    application server from the management server

    Closes gh-8348