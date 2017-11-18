commit cc7e8f55583b91c29dc1375160d7f7d2403590ff
Author: Sebastien Deleuze <sdeleuze@gopivotal.com>
Date:   Thu Jun 19 11:40:14 2014 +0200

    Support Java and MVC namespace view resolution config

    This commit improves and completes the initial MVC namespace
    view resolution implementation. ContentNegotiatingViewResolver
    registration is now also supported.

    Java Config view resolution support has been added.
    FreeMarker, Velocity and Tiles view configurers are registered
    depending on the classpath thanks to an ImportSelector.

    For both, a default configuration is provided and documented.

    Issue: SPR-7093