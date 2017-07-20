commit 1d91fd2dd63cd7fcbe8a9d462370d909904eebe7
Author: Timo Besenreuther <timo.besenreuther@gmail.com>
Date:   Wed Apr 10 15:25:51 2013 +0200

    refs #1700 time tracking

     * bug fix: min/max generation time didn't work in nested data tables because the column aggregation operations were not passed to sub tables
     * ui improvement: very small values were shown as 0s. use three decimal places if value is smaller than 10ms (e.g. display 2ms as 0.002s instead of 0s)