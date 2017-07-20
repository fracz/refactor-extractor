commit 3886c47ebe1700009c89df9c25f2df94ea59236f
Author: Mark Baker <mbaker@inviqa.com>
Date:   Fri Feb 15 15:42:06 2013 +0000

    Refactoring of calculation engine using the multiton pattern to eliminate caching issues when working with multiple workbooks
    Refactoring of calculation engine for improved performance and memory usage
    Refactoring of cell object to eliminate data duplication and reduce memory