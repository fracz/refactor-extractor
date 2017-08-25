commit ac2e05928ae3d7e03e2d8fb751e3e7ebf08ebfd7
Author: François B <github@bonzon.com>
Date:   Sat Oct 3 11:03:22 2015 +0200

    Improved driver_stats() for sqlite driver

    - Use realpath() to merge possible consecutive // inside path
    - Improve info returned by driver_stats() for files driver
    - Fix bug in driver_stats() for sqlite driver. indexing table would
    reset the size
    - Create 2 class constants in sqlite driver to improve readability