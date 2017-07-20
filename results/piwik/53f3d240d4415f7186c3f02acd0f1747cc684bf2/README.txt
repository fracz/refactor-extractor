commit 53f3d240d4415f7186c3f02acd0f1747cc684bf2
Author: Benaka Moorthi <benaka.moorthi@gmail.com>
Date:   Sat Aug 31 04:39:06 2013 -0400

    Refs #4041, refactored jqplot datatable visualizations (mainly the evolution graph),

    Notes:
      - Fix bugs in DataTable class where getRowFromLabel would fail if summary row label is returned.
      - Fix regression caused by datatable visualization refactor (add_total_row property was effectively ignored).
      - Added x_axis_step_size property to jqplot graph.