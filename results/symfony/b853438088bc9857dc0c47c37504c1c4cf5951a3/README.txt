commit b853438088bc9857dc0c47c37504c1c4cf5951a3
Author: Tobias Schultze <webmaster@tubo-world.de>
Date:   Wed Sep 4 17:33:26 2013 +0200

    [Templating] deprecate DebuggerInterface

    set logger in extension , so its only done in debug mode
    add psr/log to suggest of templating
    add test for setLogger and refactor tests to not depend that much an
    deprecated functionality