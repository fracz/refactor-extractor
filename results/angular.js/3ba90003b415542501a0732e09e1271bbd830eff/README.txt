commit 3ba90003b415542501a0732e09e1271bbd830eff
Author: Igor Minar <iiminar@gmail.com>
Date:   Fri Aug 26 10:54:38 2011 -0700

    fix(test): improve $cookie service test to work with Safari 5.1

    the max size for safari cookies has changed sligtly so I had to adjust
    the test to make cookie creation fail on this browser