commit a45e4f6eb4d0b7a488f92910a5a660c8a3dcc519
Author: Mike van Riel <me@mikevanriel.com>
Date:   Sun Jun 1 09:46:08 2014 +0200

    User-specific configuration was not loaded

    During the recent refactoring of the configuration system the --config
    and -c options no longer worked because of an error in the loading
    of said configuration. This commit resolves that bug