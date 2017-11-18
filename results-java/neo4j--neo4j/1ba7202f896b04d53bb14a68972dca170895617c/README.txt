commit 1ba7202f896b04d53bb14a68972dca170895617c
Author: Jacob Hansson <jakewins@gmail.com>
Date:   Fri Dec 4 15:11:19 2015 -0600

    Make `Settings` utility class internal

    Adding new configuration options to Neo4j is not a public API. This
    commit deprecates the Settings public utility class in favor of an internal
    equivalent class.

    The old class does not inherit from the new one, since the intent of this
    change (beyond cleaning up surface API) is to allow refactoring the
    now-internal Settings class.