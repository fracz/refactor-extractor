commit de8913e2f5b7c44b83963905a6c20009e6f85567
Author: robert.koch@loggia.at <robert.koch@loggia.at>
Date:   Wed May 30 20:29:50 2012 +0000

    fixed problems that came up with Autobahn test suite
    do clean shutdown of WebSocket connection
    forward each frame separately instead of waiting for finished message for performance reasons
    fix continous read if given buffer is not full
    improved logging