commit 257139c60b8700b7fdfc495ec84ef63e24b2dde4
Author: SteveJobzniak <SteveJobzniak@users.noreply.github.com>
Date:   Sun Apr 16 21:30:30 2017 +0200

    setUser(): Refactoring to avoid wiping device

    The recently introduced advertising_id (aka "adid") was written in a way
    that caused the whole device to be nuked and re-generated whenever
    anybody migrated to the new commit/version of this library. Ouch.

    We shouldn't handle new parameters that way since we must always try
    to create as few device IDs as possible (Instagram silently blacklists
    IPs after they see too many devices from it). We should try to always
    keep the same hardware ID in most cases.

    Therefore, the code has now been refactored so that non-critical, new
    parameters such as "advertising_id" are added in a separate section of
    setUser(), which happens AFTER the main "wipe everything" section.

    The "nuke it all" wipe now only happens when the user has gotten a brand
    new "devicestring" or if they're missing any of the critically important
    trio of parameters (uuid, phone_id or device_id). For everything else,
    the new parameters are seamlessly added onto the user's existing device!

    :-)