commit 1a148fb681762d7177b7bda0fa852bb06492b79b
Author: Thomas Steur <thomas.steur@gmail.com>
Date:   Thu Mar 5 04:35:00 2015 +0000

    Faster archiving of aggregated reports, also performance imprvovements in general

    * Store subtables in chunks of 100 subtables per blob. Those 100 subtables are stored
      serialized as an array: array($subtableID => subtableBlob). The first 100 subtables are
      stored in "chunk_0", the next 100 subtables are stored in "chunk_1", ...
    * Subtable Ids are now consecutive from 1 to X
    * We do no longer serialize the whole Row instance when archiving, instead we only
      serialize the Row's array which contains columns, metadata and datatable. This is not
      only more efficient but allows us to refactor the Row instance in the future (although
      we will always have to be BC)
    * Faster row implementation: Columns, Metadata and Subtables access is much faster now