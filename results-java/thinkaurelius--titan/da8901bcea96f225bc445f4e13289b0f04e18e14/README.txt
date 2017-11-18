commit da8901bcea96f225bc445f4e13289b0f04e18e14
Author: Matthias Broecheler <me@matthiasb.com>
Date:   Tue Oct 2 19:37:59 2012 -0700

    refactored ID acquisition to make it more robust and faster. Added test cases to test multi-threaded id acquisition through two different storage manager instances.

    IMPORTANT: This commit changes the way id blocks are written to the storage backend. Hence, this change is NOT compatible with previous Titan versions. If you want to upgrade an existing Titan deployment, please wait for the next release.