commit ff2c0e7b4cef6c026fd42cafb371a3d51a363e68
Author: rvesse <rvesse@dotnetrdf.org>
Date:   Wed Mar 11 16:15:16 2015 +0000

    Fix Tachyon host name resolution (TACHYON-303)

    Under some network configurations it is possible for Tachyon host name
    resolution for the local host to select a host that is not actually
    reachable or usable, for example a network broadcast address.  When it
    does so Tachyon will hang until such time as the timeout and retry
    limits are reached which can be an extremely long time.

    This commit improves the local host resolution to ensure that the host
    selected is actually reachable and to discount any broadcast addresses
    from use as local hosts.

    As part of this it was necessary to add a timeout to the host name
    resolution so that it doesn't spend too long waiting to determine if a
    particular host name is reachable.  This defaults to 5000 milliseconds
    currently, the relevant methods were updated to take a timeout and all
    relevant calls updated to pass in the timeout value.

    Since host names and IPs shouldn't change over the life of the JVM and
    since Tachyon typically only does this resolution once at start up time
    we also now cache these values.  This is particularly important when
    running the tests under affected network configurations as otherwises
    the tests will take substantially longer than normal.