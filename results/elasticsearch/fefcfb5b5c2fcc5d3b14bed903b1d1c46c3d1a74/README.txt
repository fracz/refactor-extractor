commit fefcfb5b5c2fcc5d3b14bed903b1d1c46c3d1a74
Author: kimchy <kimchy@gmail.com>
Date:   Sun Jul 18 22:54:21 2010 +0300

    refactor recovery to be handled on the node level (and not per shard), with better retry mechanism when doing peer shard recovery