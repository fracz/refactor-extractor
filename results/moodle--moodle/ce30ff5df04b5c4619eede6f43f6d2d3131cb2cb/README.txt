commit ce30ff5df04b5c4619eede6f43f6d2d3131cb2cb
Author: Dan Poltawski <dan@moodle.com>
Date:   Wed May 30 23:56:07 2012 +0800

    MDL-29857 google apis: OAuth 2.0 repository plugins upgrade

    We are disabling the plugins on upgrade and emailing admins
    about this if they have a plugin configured.

    This is required because OAuth 2 credentials are now required
    in the plugin configuration.

    NOTE: These strings are temporary and need to be improved.