commit 2d98f69e937673890d2cdb30d82a849217b2dc7e
Author: Artem Prigoda <arteamon@gmail.com>
Date:   Sun Apr 26 19:05:19 2015 +0300

    Improve DropwizardApacheConnectorTest

    * Expect NoRouteException as well as ConnectionTimeoutException.
    If a local network is configured to reject request to non-routable
    addresses, then this exception is returned by JVM. We should
    handle such situations.
    * Add a health check, so the application doesn't yell on us in logs.
    * Add code style and documentation improvements.