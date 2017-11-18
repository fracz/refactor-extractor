commit a3a23a8d53117ce1792523f5e6fde126b6efb48c
Author: Sam Judd <sam@bu.mp>
Date:   Wed Jan 9 14:11:16 2013 -0800

    Recycle inTempStorage and buffered input streams

    Drastically reduces the number of GC_FOR_ALLOCs
    when the number of resize operations is high (as
    in a grid view of photos with lots of columns).
    Also resuses more code and refactors out some
    methods