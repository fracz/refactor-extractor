commit eff855ce169a8b81391ddcb10f4acc00a5f0915b
Author: Chris Gioran <chris.gioran@neotechnology.com>
Date:   Sat Jul 12 18:39:05 2014 +0200

    Final changes for migrating directly from 1.9 stores

    Introduces the LegacyStore abstraction for reading from legacy stores.
     The existing 2.0 implementation is renamed as such and the 1.9
     version implementation is added and improved to have the same
     interface.
    The check for the migration checks the version and instantiates the
     correct version legacy store.