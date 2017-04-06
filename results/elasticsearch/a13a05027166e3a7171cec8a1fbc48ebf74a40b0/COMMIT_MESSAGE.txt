commit a13a05027166e3a7171cec8a1fbc48ebf74a40b0
Author: Nik Everett <nik9000@gmail.com>
Date:   Fri Nov 4 20:59:15 2016 -0400

    Add automatic parallelization support to reindex and friends (#20767)

    Adds support for `?slices=N` to reindex which automatically
    parallelizes the process using parallel scrolls on `_uid`. Performance
    testing sees a 3x performance improvement for simple docs
    on decent hardware, maybe 30% performance improvement
    for more complex docs. Still compelling, especially because
    clusters should be able to get closer to the 3x than the 30%
    number.

    Closes #20624