commit 6bd745320619dc43fec2326582c5f5b2f3c9a740
Author: Erik Kline <ek@google.com>
Date:   Fri May 19 10:10:41 2017 +0900

    Minor improvement in logging in TetheringConfiguration

    Test: as follows
        - built
        - booted
        - flashed
        - runtest frameworks-net passes
        - dumpsys connectivity shows expected output
    Bug: 32163131
    Bug: 36504926
    Bug: 36988090
    Bug: 38152109
    Bug: 38186915
    Bug: 38218697
    Change-Id: I4a2129d780dfec7bca693486a100ea3c78465430