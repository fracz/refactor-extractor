commit 0b02551e2f8f8ae7107b2303c10432542a201fbe
Author: Rossen Stoyanchev <rstoyanchev@pivotal.io>
Date:   Thu Aug 28 22:32:38 2014 -0400

    Update resource handler Java config

    This change separates out resource chain related methods previously in
    ResourceHandlerRegistration into a new class ResourceChainRegistration
    with the goal of improving readability.

    Along with that, the registration of caching resolver and transformer
    is now explicitly controled with a boolean flag (on the method used
    to obtain the ResourceChainRegistration) and an overloaded method
    also allows providing a Cache instance.

    Issue: SPR-12124