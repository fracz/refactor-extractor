commit c0229149cbaa2a11d1d79e7c23a44b3511ac164b
Author: nitsanw <nitsanw@yahoo.com>
Date:   Fri May 26 16:04:37 2017 +0200

    [Java] align first frame on replay

    - Write a padding frame into first segment file to enable
    - Some refactoring in Recorder for better naming
    - Expose actual replay start point from reader to be used on replay pub creation