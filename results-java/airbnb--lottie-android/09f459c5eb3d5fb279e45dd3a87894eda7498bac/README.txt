commit 09f459c5eb3d5fb279e45dd3a87894eda7498bac
Author: Eli Hart <konakid@gmail.com>
Date:   Fri Feb 24 18:17:52 2017 -0800

    Add tests for changing compositions (#164)

    Adds a test for running one composition, and then setting a new composition on the same view and running that one.

    Adds a second test for setting the same composition twice in a row on the same view and making sure it runs as expected the second time.

    I refactored the robot a bit to reuse the test setup code.