commit a22776e9543442ed18b64ceeec84029878d7716f
Author: Paul Arthur <paul.arthur@flowerysong.com>
Date:   Wed Jul 11 14:42:30 2012 -0400

    Rework debug page

    We don't need to check for PCRE any more, it can't be disabled in PHP
    5.3.

    Try to improve the descriptions.