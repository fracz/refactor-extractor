commit 7badd2c402f9e8e9fd13f6915ad2e32301f9f305
Author: Mathias Agopian <mathias@google.com>
Date:   Mon Nov 22 15:48:10 2010 -0800

    allow rotation-vector to have 4 components

    - upadte documentation for rotation vector
    - update method dealing with rotation vector to deal with 4 components
    - virtual rotation-vector sensor reports all four components
    - improve SensorManager documentation layout

    Whent he 4-th component of the rotation-vector is present, we can save
    a square-root when computing the quaternion or rotation matrix from it.

    Change-Id: Ia84d278dd5f0909fab1c5ba050f8df2679e2c7c8