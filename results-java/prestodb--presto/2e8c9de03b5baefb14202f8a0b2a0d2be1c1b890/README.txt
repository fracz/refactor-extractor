commit 2e8c9de03b5baefb14202f8a0b2a0d2be1c1b890
Author: Henning Schmiedehausen <hgschmie@fb.com>
Date:   Tue Jun 25 17:35:16 2013 -0700

    Make refresh materialized view work again

    The refactored connector code is more picky with ColumnHandles. Build the set of source and destination columns
    separately from the respective TableMetadata.