commit 9a4c3287d1940cb1ea0e15baecb3d8cfe6c514aa
Author: Daniel Cohen Gindi <danielgindi@gmail.com>
Date:   Mon Jun 29 21:51:33 2015 +0300

    Fixed an issue where highlights disappear on multi-datasets

    They disappeared because of NaN values introduces in the previous iteration, when the previous dataset does not contain a yValue for that specific index.

    (And a little refactoring to make this thing consistent across all appearances)