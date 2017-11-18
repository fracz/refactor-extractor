commit 949e65165cddf9062e059d40b2c13608713cb993
Author: Roman Leventov <leventov@users.noreply.github.com>
Date:   Wed Dec 7 17:49:16 2016 -0600

    Bitset iteration optimization and improve safety (#3753)

    * Deduplicate looking for bitset.nextSetBit() in BitSetIterator.next() and hasNext()

    * Add BitmapIterationTest

    * More elaborate comment on why Roaring is not tested in BitmapIterationTest