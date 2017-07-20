commit 612c29cff6031ecdabffa7cf5faa99ad2e614a7f
Author: Greg Dunlap <gdd@heyrocker.com>
Date:   Wed Feb 22 01:07:21 2012 -0800

    Added an exception handler to config->get() which handles situations where the db is not in place, allowing us to remove a call to db_table_exists() which should improve cached performance a bit