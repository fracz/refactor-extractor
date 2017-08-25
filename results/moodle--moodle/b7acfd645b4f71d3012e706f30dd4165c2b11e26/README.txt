commit b7acfd645b4f71d3012e706f30dd4165c2b11e26
Author: Dan Poltawski <dan@moodle.com>
Date:   Tue May 29 14:35:25 2012 +0800

    MDL-29857 google apis: OAuth 2.0 portfolio plugins upgrade

    We are disabling the plugins if not configured. As part of
    this upgrade we will emailing admins about this change
    if they have a plugin configured.

    This is required because OAuth 2 credentials are now required
    in the plugin configuration.

    NOTE: These strings are temporary and need to be improved.