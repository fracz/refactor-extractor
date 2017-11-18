commit 18fab29c234a68ebb23465f53b517e0f0e10044b
Author: Carlos Alexandro Becker <caarlos0@gmail.com>
Date:   Sun Apr 24 16:31:46 2016 -0300

    improved tests readbility a bit

    all tests were doing stuff like `try { something(); } catch (Exception e) { fail(); }`, I removed those try-catch blocks were I thought was safe to.

    I also removed some commented out code..