commit 08fec7bdf846daa3dfa4114310f065294ac092fc
Author: Andrey Andreev <narf@devilix.net>
Date:   Fri Jul 19 16:25:51 2013 +0300

    Router improvements

    - Make dashes-to-underscores URI segment replacement configurable via ['translate_uri_dashes'].
    - Make _set_routing() protected and move the call to the class constructor.
    - Remove redudant calls to set_class() and set_method().
    - Clean-up/optimize the routes loading procedure.

    (fixes issue #2503)