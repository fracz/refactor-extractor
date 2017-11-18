commit be7c50e0a14e91330ce13161bc14a33d34ff6aca
Author: Dianne Hackborn <hackbod@google.com>
Date:   Mon Jun 30 14:43:28 2014 -0700

    Add network access blocking when in battery save mode.

    The network policy manager now monitors battery save mode and,
    when in battery save, uses its facility to block access to metered
    networks to block access to all networks.  That is, it tells the
    network management service that all networks have an (infinite)
    quota, and puts various app uids to be restricted under quota
    interfaces as appropriate.

    This new network blocking needs a new facility to be able to white
    list apps, such as GmsCore.  To do this, I refactored the package
    manager's permission configuration stuff into a separate SystemConfig
    class that can be used by others, and it now has a new tag to
    specify package names that should be white-listed for power save
    mode.  These are retrieved by the network policy manager and used
    to build a whitelist of uids.

    The new general config files can now go in system/etc/config,
    though currently everything still remains in the permissions dir.

    Still left to be done is changing the semantics of what uids are
    allowed in this mode, to include all perceptable uids.  (So that we
    can still do things like background music playback.)  This will be
    done in a follow-on CL.

    Change-Id: I9bb7029f61dae62e6236da5ca60765439f8d76d2