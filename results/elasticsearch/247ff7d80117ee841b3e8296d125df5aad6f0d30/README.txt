commit 247ff7d80117ee841b3e8296d125df5aad6f0d30
Author: Alexander Reelsen <alexander@reelsen.net>
Date:   Fri Aug 15 09:42:27 2014 +0200

    Transport: Refactor guice startup

    * Removed & refactored unused module code
    * Allowed to set transports programmatically
    * Allow to set the source of the changed transport

    Note: The current implementation breaks BWC as you need to specify a concrete
    transport now instead of a module if you want to use a different
    Transport or HttpServerTransport

    Closes #7289