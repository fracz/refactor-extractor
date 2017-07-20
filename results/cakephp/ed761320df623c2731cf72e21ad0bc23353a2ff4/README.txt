commit ed761320df623c2731cf72e21ad0bc23353a2ff4
Author: Saleh Souzanchi <saleh.souzanchi@gmail.com>
Date:   Tue Dec 25 04:30:07 2012 +0330

    Disabled callbacks in _getMax() & _getMin()

    This fixes issues where model/behavior callbacks append into the query
    conditions without checking that its an array. Disabling callbacks
    should also improve performance a tiny bit.

    Refs #GH-1049