commit cc2e4fb2adfd3cbdc013add1545d09091ce07d44
Author: Andrey Lomakin <lomakin.andrey@gmail.com>
Date:   Mon May 6 13:10:25 2013 +0300

    Issue #1404  1. Issue wih OFileClassic softly closed detection was fixed (OS caches were not taken in account).  2. Unsafe direct memory implementation was replaced by JNA because of JIT related issues. 3. Few multithreading improvements for OFile implementations.