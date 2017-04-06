commit a4e4feed909408ff837ac328371d0fad6ca49e9d
Author: Peter Bacon Darwin <pete@bacondarwin.com>
Date:   Tue May 24 18:43:52 2016 +0100

    refactor(*): faster check for a string starting with a substring

    Thanks to @spamdaemon for the original PR to make this improvement.

    In naive tests on Chrome I got the following results:

    ```
                 Matches   Misses
    indexOf      33ms      1494ms
    lastIndexOf  11ms      11ms
    ```

    Closes #3711