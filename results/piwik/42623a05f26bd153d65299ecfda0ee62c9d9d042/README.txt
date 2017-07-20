commit 42623a05f26bd153d65299ecfda0ee62c9d9d042
Author: Benaka Moorthi <benaka.moorthi@gmail.com>
Date:   Sun Sep 1 11:26:59 2013 -0400

    Refs #4041, refactored jqplot.js file by removing JQPlot class and creating JqplotGraph datatable class, and did more refactoring to jqplot data generating mechanism.

    Notes:
      - Removed jqplot specific code from datatable_manager.js and moved to new JqplotGraph class.
      - Moved tooltip percentage calculating code to client.