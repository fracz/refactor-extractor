commit e7c1e7a81943e7f0e64a959efbd670eac4126ff6
Author: Pontus Melke <pontusmelke@gmail.com>
Date:   Mon Oct 17 21:18:31 2016 +0200

    Some unpack errors should be sent to client

    After the state-machine refactoring we no longer send back fatal/external error
    to the client but only abruptly close the connection. In some cases such as invalid
    parameters to a run statement we should send back a proper error to the client and not
    just silently fail.