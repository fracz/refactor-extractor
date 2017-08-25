commit f337e4211c7323b0de82988780bf4cc483121992
Author: Greg Brockman <gdb@gregbrockman.com>
Date:   Mon Dec 12 16:29:51 2011 -0800

    Internally, use require instead of require_once

    This yields a performance improvement in certain frameworks. Thanks to
    Phil Freo for pointing this out.