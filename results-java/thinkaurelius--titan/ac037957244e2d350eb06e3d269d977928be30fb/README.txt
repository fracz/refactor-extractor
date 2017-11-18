commit ac037957244e2d350eb06e3d269d977928be30fb
Author: Matthias Broecheler <me@matthiasb.com>
Date:   Wed Jun 27 14:36:56 2012 -0700

    Added clearStorage() method to StorageManager for clearing out the database and refactored test cases to use this method which makes the tests cleaner and simpler.
    Added some configuration options to Astyanax. InternalCassandra tests not working.